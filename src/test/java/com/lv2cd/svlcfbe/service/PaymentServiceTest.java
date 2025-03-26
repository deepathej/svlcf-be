package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.UserType.CONSUMER;
import static com.lv2cd.svlcfbe.util.CommonMethods.*;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.DelTrans;
import com.lv2cd.svlcfbe.entity.Payment;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.repository.DeletedTransactionRepo;
import com.lv2cd.svlcfbe.repository.PaymentRepository;
import com.lv2cd.svlcfbe.repository.PreBalanceRepository;
import com.lv2cd.svlcfbe.service.generatepdf.PreBalanceReportService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private SvlcfService svlcfService;
  @Mock private UserService userService;
  @Mock private PreBalanceReportService preBalanceReportService;
  @Mock private PreBalanceRepository preBalanceRepository;
  @Mock private DeletedTransactionRepo deletedTransactionRepo;
  @InjectMocks private PaymentService paymentService;

  @Test
  void testNewPaymentMethodWhenValidDetailsAreSentThenDataIsSavedInDB() {
    Payment payment = getPaymentReq();
    User user = getConsumerWithBalance();
    Payment expectedPayment = getPayment();
    when(userService.updateUserBalance(
            payment.getUserId(),
            payment.getPaymentAmount(),
            user.getBalance(),
            payment.getPaymentType()))
        .thenReturn(user.getBalance() - payment.getPaymentAmount());
    when(svlcfService.getAvailableIdByName(PAYMENT_STRING)).thenReturn(expectedPayment.getId());
    payment.setDate(getCurrentDate());
    payment.setTime(getCurrentTime());
    expectedPayment.setDate(getCurrentDate());
    expectedPayment.setTime(getCurrentTime());
    when(paymentRepository.save(payment)).thenReturn(payment);
    Payment actualPayment = paymentService.newPayment(payment);
    Assertions.assertEquals(expectedPayment, actualPayment);
  }

  @Test
  void testGetPaymentForTheDateMethodWhenValidDateIsSentThenMatchingDataIsRetrieved() {
    String date = getCurrentDate();
    when(paymentRepository.findByDate(date)).thenReturn(List.of(getPayment()));
    List<Payment> actualPayment = paymentService.getPaymentForTheDate(date);
    Assertions.assertEquals(I_ONE, actualPayment.size());
  }

  @Test
  void
      testFindPaymentRecordsByUserIdMethodWhenValidUserIdIsSentThenPaymentDetailsForTheUserIsRetrieved() {
    when(paymentRepository.findByUserId(L_TEN_THOUSAND)).thenReturn(List.of(getPayment()));
    List<Payment> actualPayment = paymentService.getPaymentRecordsByUserId(L_TEN_THOUSAND);
    Assertions.assertEquals(I_ONE, actualPayment.size());
  }

  @Test
  void testDeletePaymentByIdMethodWhenPaymentByIdIsNotAvailableThenExceptionIsThrown() {
    when(paymentRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> paymentService.deletePaymentById(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + PAYMENT_STRING, thrown.getMessage());
  }

  @Test
  void testDeletePaymentByIdMethodWhenNotLastEntryThenExceptionIsThrown() {
    when(paymentRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(getPayment()));
    when(paymentRepository.findByUserId(L_TEN_THOUSAND_AND_ONE))
        .thenReturn(List.of(getPayment(), getPayment2()));
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> paymentService.deletePaymentById(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(ONLY_USER_LAST_PAYMENT_CAN_BE_DELETED, thrown.getMessage());
  }

  @Test
  void testDeletePaymentByIdMethodWhenLastEntryThenEntryIsDeletedFromDB() {
    Payment payment = getPayment();
    when(svlcfService.getAvailableIdByName(DEL_TRANS_STRING)).thenReturn(L_TEN_THOUSAND);
    DelTrans delTrans = new DelTrans(L_TEN_THOUSAND, PAYMENT_STRING, writeAsJson(payment));
    when(paymentRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(payment));
    when(paymentRepository.findByUserId(L_TEN_THOUSAND_AND_ONE)).thenReturn(List.of(payment));
    when(deletedTransactionRepo.save(delTrans)).thenReturn(delTrans);
    doNothing().when(userService).directUpdateUserBalance(L_TEN_THOUSAND_AND_ONE, I_TEN_THOUSAND);
    doNothing().when(paymentRepository).deleteById(L_TEN_THOUSAND);
    assertEquals(SUCCESS, paymentService.deletePaymentById(L_TEN_THOUSAND));
  }

  @Test
  void testGetPreviousBalanceMethodWhenDataAvailableForConsumerThenReportIsGenerated() {
    when(preBalanceRepository.findAll())
        .thenReturn(List.of(getConsumerWithPreBalance(), getSupplierWithPreBalance()));
    User user = getSupplierWithBalance();
    when(userService.getAllUsers()).thenReturn(List.of(getConsumerWithBalance(), user));
    Map<String, String> userDetailsAndPreBalanceMap = getPreBalanceMap();
    userDetailsAndPreBalanceMap.remove(user.getId() + UNDER_SCORE + user.getName());
    when(preBalanceReportService.generatePreBalanceReport(userDetailsAndPreBalanceMap, CONSUMER))
        .thenReturn(SUCCESS);
    assertEquals(SUCCESS, paymentService.getPreviousBalance(CONSUMER));
  }

  @Test
  void testGetPaymentsThenAvailablePaymentsAreReturned() {
    when(paymentRepository.findAll())
        .thenReturn(List.of(getPayment(), getPayment2(), getPayment3()));
    assertEquals(I_THREE, paymentService.getPayments().size());
  }
}
