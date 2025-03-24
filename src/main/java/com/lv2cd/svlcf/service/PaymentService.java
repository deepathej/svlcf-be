package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.CommonMethods.getCurrentTime;
import static com.lv2cd.svlcf.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.DelTrans;
import com.lv2cd.svlcf.entity.Payment;
import com.lv2cd.svlcf.entity.User;
import com.lv2cd.svlcf.enums.UserType;
import com.lv2cd.svlcf.exception.CustomBadRequestException;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import com.lv2cd.svlcf.repository.DeletedTransactionRepo;
import com.lv2cd.svlcf.repository.PaymentRepository;
import com.lv2cd.svlcf.repository.PreBalanceRepository;
import com.lv2cd.svlcf.service.generatepdf.PreBalanceReportService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentService {

  private SvlcfService svlcfService;
  private UserService userService;
  private PaymentRepository paymentRepository;
  private PreBalanceRepository preBalanceRepository;
  private PreBalanceReportService preBalanceReportService;
  private DeletedTransactionRepo deletedTransactionRepo;

  /**
   * Adds new Payment to the DB
   *
   * @param payment without id
   * @return payment object with id
   */
  public Payment newPayment(Payment payment) {
    log.info("Adding new Payment {}", writeAsJson(payment));
    payment.setId(svlcfService.getAvailableIdByName(PAYMENT_STRING));
    payment.setBalanceAmount(
        userService.updateUserBalance(
            payment.getUserId(),
            payment.getPaymentAmount(),
            payment.getPreviousBalance(),
            payment.getPaymentType()));
    payment.setDate(getCurrentDate());
    payment.setTime(getCurrentTime());
    return paymentRepository.save(payment);
  }

  /**
   * List of Payments based on the provided Date
   *
   * @param date for retrieving the details
   * @return list of payments
   */
  public List<Payment> getPaymentForTheDate(String date) {
    log.info("Getting the list of payments for the date {}", date);
    return paymentRepository.findByDate(date);
  }

  /**
   * Gets list of All the Payment records
   *
   * @return list of Payments
   */
  public List<Payment> getPayments() {
    log.info("Getting the list of all payments");
    return paymentRepository.findAll();
  }

  /**
   * Gets the list of the Payments based on the userId
   *
   * @param userId unique id for User
   * @return list of Payments
   */
  public List<Payment> getPaymentRecordsByUserId(Long userId) {
    log.info("Getting the list of payments for the userId {}", userId);
    return paymentRepository.findByUserId(userId);
  }

  /**
   * Deletes the last payment based on the userId
   *
   * @param id unique id for payment
   * @return success message
   */
  public String deletePaymentById(Long id) {
    log.info("deleting payment record based on id {}", id);
    Payment payment =
        paymentRepository
            .findById(id)
            .orElseThrow(
                () -> new CustomInternalServerException(FAILED_DB_OPERATION + PAYMENT_STRING));
    if (isNotLastPaymentForUser(id, payment.getUserId())) {
      throw new CustomBadRequestException(ONLY_USER_LAST_PAYMENT_CAN_BE_DELETED);
    }
    DelTrans delTrans =
        new DelTrans(
            svlcfService.getAvailableIdByName(DEL_TRANS_STRING),
            PAYMENT_STRING,
            writeAsJson(payment));
    userService.directUpdateUserBalance(payment.getUserId(), payment.getPreviousBalance());
    deletedTransactionRepo.save(delTrans);
    paymentRepository.deleteById(id);
    return SUCCESS;
  }

  /**
   * @param id unique id for payment
   * @param userId unique id for User
   * @return boolean value to represent if this is the last payment or not
   */
  private boolean isNotLastPaymentForUser(Long id, Long userId) {
    log.info("Checking if the payment is not last payment made by the userId {}", userId);
    return getPaymentRecordsByUserId(userId).stream().anyMatch(payment1 -> payment1.getId() > id);
  }

  /**
   * Get the previous balance report based on the userType
   *
   * @param userType consumer/supplier
   * @return report path
   */
  public String getPreviousBalance(UserType userType) {
    log.info("Creating previous balance for the userType {}", userType);
    Map<Long, User> userMap =
        userService.getAllUsers().stream()
            .filter(user -> user.getUserType() == userType)
            .collect(Collectors.toMap(User::getId, user -> user));
    Map<String, String> userDetailsAndPreBalanceMap =
        preBalanceRepository.findAll().stream()
            .map(
                preBalance -> {
                  User user = userMap.get(preBalance.getUserId());
                  if (user != null && preBalance.getBalanceAmount() != null) {
                    return Map.entry(
                        user.getId() + UNDER_SCORE + user.getName(),
                        preBalance.getBalanceAmount().toString());
                  }
                  return null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return preBalanceReportService.generatePreBalanceReport(userDetailsAndPreBalanceMap, userType);
  }
}
