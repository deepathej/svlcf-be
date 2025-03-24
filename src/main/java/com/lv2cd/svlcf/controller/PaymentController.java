package com.lv2cd.svlcf.controller;

import static com.lv2cd.svlcf.enums.PaymentType.PAYMENT;
import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.Payment;
import com.lv2cd.svlcf.enums.UserType;
import com.lv2cd.svlcf.service.PaymentService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class PaymentController {

  private PaymentService paymentService;

  /**
   * API is used to save the new Payment record to the DB
   *
   * @param payment without id
   * @return payment object including the id
   */
  @PostMapping(
      value = NEW_PAYMENT_ENDPOINT,
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON)
  public ResponseEntity<Payment> newPayment(@RequestBody Payment payment) {
    payment.setPaymentType(PAYMENT);
    return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.newPayment(payment));
  }

  /**
   * API is used to get all the payments for the day
   *
   * @return list of all the payments for the day
   */
  @GetMapping(value = GET_TODAY_PAYMENTS_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<Payment>> getTodayPayments() {
    return ResponseEntity.ok(paymentService.getPaymentForTheDate(getCurrentDate()));
  }

  /**
   * API is used to delete only the last payment from the DB for the user, this is use-full when
   * there is a mistake in the entry
   *
   * @param id unique id for the payment
   * @return success message
   */
  @DeleteMapping(DELETE_PAYMENT_BY_ID_ENDPOINT)
  public ResponseEntity<String> deletePaymentById(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(paymentService.deletePaymentById(id));
  }

  /**
   * API is not used in the frontend
   *
   * @param userType consumer/supplier
   * @return PDF link
   */
  @GetMapping(value = GET_PREVIOUS_BALANCE_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<String> getPreviousBalance(@PathVariable UserType userType) {
    return ResponseEntity.ok(paymentService.getPreviousBalance(userType));
  }
}
