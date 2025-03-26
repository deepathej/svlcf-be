package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentTime;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.entity.*;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserReportServiceTest {

  @InjectMocks private UserReportService userReportService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGenerateUserReportMethodWhenInvalidFilePathThenExceptionIsThrown() {
    User user = getConsumerWithBalance();
    PreBalance preBalance = getConsumerWithPreBalance();
    List<Product> productList = getProducts();
    List<Payment> payments = getPayments();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                userReportService.generateUserReport(
                    user, productList, payments, preBalance, false),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + USER_REPORT_PATH.replace(SLASH, BACK_SLASH)
            + L_TEN_THOUSAND_AND_ONE
            + TEMP
            + ERROR_EXTENSION_FOR_PDF_GENERATORS,
        thrown.getMessage());
  }

  @Test
  void
      testGenerateUserReportMethodWhenProductPaymentListsAreEmptyAndPreBalanceIsNullForNonMultiRequestThenExceptionIsThrown() {
    User user = getConsumerWithBalance();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> userReportService.generateUserReport(user, List.of(), List.of(), null, false),
            EXCEPTION_NOT_THROWN);
    assertEquals(NO_TRANSACTIONS_FOR_THE_USER + user.getId(), thrown.getMessage());
  }

  @Test
  void
      testGenerateUserReportMethodWhenProductPaymentListsAreEmptyAndPreBalanceIsNullForMultiRequestThenExceptionIsThrown() {
    User user = getConsumerWithBalance();
    String errorMessage =
        userReportService.generateUserReport(user, List.of(), List.of(), null, true);
    assertEquals(NO_TRANSACTIONS_FOR_THE_USER + user.getId(), errorMessage);
  }

  @Test
  void testGenerateUserReportMethodWhenValidFilePathThenUserReportIsGenerated() {
    User user = getConsumerWithBalance();
    user.setBalance(I_SEVEN_THOUSAND_AND_EIGHT_HUNDRED);
    PreBalance preBalance = getConsumerWithPreBalance();
    List<Product> productList = getProducts();
    List<Payment> payments = getPayments();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String userReportPath =
        userReportService.generateUserReport(user, productList, payments, preBalance, false);
    assertNotNull(userReportPath);
    validateAndDeleteFile(userReportPath);
  }

  @Test
  void testGenerateUserReportMethodWhenValidFilePathAndPreBalanceIsNullThenUserReportIsGenerated() {
    User user = getConsumerWithBalance();
    user.setBalance(-I_TWO_THOUSAND_TWO_HUNDRED);
    List<Product> productList = getProducts();
    List<Payment> payments = getPayments();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String userReportPath =
        userReportService.generateUserReport(user, productList, payments, null, false);
    assertNotNull(userReportPath);
    validateAndDeleteFile(userReportPath);
  }

  @Test
  void testGenerateUserReportMethodWhenDataMismatchThenExceptionIsThrown() {
    User user = getConsumerWithBalance();
    List<Product> productList = getProducts();
    List<Payment> payments = getPayments();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> userReportService.generateUserReport(user, productList, payments, null, false),
            EXCEPTION_NOT_THROWN);
    assertEquals(DB_DATA_ISSUE + USER_BALANCE_MISMATCH, thrown.getMessage());
  }

  private List<Payment> getPayments() {
    return Stream.of(getPayment(), getPayment2())
        .peek(
            payment -> {
              payment.setTime(getCurrentTime());
              payment.setDate(getCurrentDate());
            })
        .toList();
  }

  private List<Product> getProducts() {
    return List.of(getProduct(), getProduct2());
  }
}
