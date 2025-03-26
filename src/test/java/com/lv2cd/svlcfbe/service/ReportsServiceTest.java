package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.*;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.BalanceListWithDate;
import com.lv2cd.svlcfbe.model.ReportsRequest;
import com.lv2cd.svlcfbe.repository.*;
import com.lv2cd.svlcfbe.service.generatepdf.BalanceReportService;
import com.lv2cd.svlcfbe.service.generatepdf.StockReportService;
import com.lv2cd.svlcfbe.service.generatepdf.UserReportService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportsServiceTest {

  @Mock private StockReportRepository stockReportRepository;
  @Mock private BalanceReportRepository balanceReportRepository;
  @Mock private BalanceReportService balanceReportService;
  @Mock private SvlcfService svlcfService;
  @Mock private ProductService productService;
  @Mock private StockService stockService;
  @Mock private StockReportService stockReportService;
  @Mock private PaymentService paymentService;
  @Mock private UserService userService;
  @Mock private ExpenseService expenseService;
  @Mock private CashDepositRepository cashDepositRepository;
  @Mock private PreBalanceRepository preBalanceRepository;
  @Mock private UserReportService userReportService;
  @Mock private SalesService salesService;
  @InjectMocks private ReportsService reportsService;

  @Test
  void testDailyReportsMethodWhenRequestedContainBlankDateThenExceptionIsThrown() {
    ReportsRequest reportsRequest = getReportsRequestWithEmptyDate();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(PLEASE_SELECT_DATE, thrown.getMessage());
  }

  @Test
  void testDailyReportsMethodWhenRequestedContainInvalidTypeThenExceptionIsThrown() {
    ReportsRequest reportsRequest = getReportsRequestWithInvalidType();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(PLEASE_SELECT_TYPE, thrown.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {DAILY_STOCK_REPORT, DAILY_BALANCE_REPORT})
  void testDailyReportsMethodWhenDailyReportRequestedForExistingPastDateThenExceptionIsThrown(
      String reportType) {
    ReportsRequest reportsRequest;
    if (reportType.equalsIgnoreCase(DAILY_BALANCE_REPORT)) {
      reportsRequest = getBalanceReportReq();
      when(balanceReportRepository.findAll()).thenReturn(List.of(getBalanceReport()));
    } else {
      reportsRequest = getStockReportReq();
      when(stockReportRepository.findAll()).thenReturn(List.of(getStockReport()));
    }
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(DUPLICATE_REPORT_FOR_PAST_DATES, thrown.getMessage());
  }

  @Test
  void
      testDailyReportsMethodWhenDailyStockCurrentDateNoReportExistsForTheDayButFailedToGetPreviousReportThenExceptionIsThrown() {
    ReportsRequest reportsRequest = getStockReportReq();
    reportsRequest.setDate(getCurrentDate());
    when(stockReportRepository.findAll()).thenReturn(List.of(getStockReport()));
    when(svlcfService.getAvailableIdByName(STOCK_REPORT_STRING)).thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(stockReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + STOCK_REPORT_STRING, thrown.getMessage());
  }

  @Test
  void
      testDailyReportsMethodWhenDailyStockCurrentDateAndNoReportExistsForTheDayButValidationFailsThenExceptionIsThrown() {
    String date = getCurrentDate();
    ReportsRequest reportsRequest = getStockReportReq();
    reportsRequest.setDate(date);
    StockReport stockReport = getStockReport();
    when(stockReportRepository.findAll()).thenReturn(List.of(stockReport));
    when(svlcfService.getAvailableIdByName(STOCK_REPORT_STRING)).thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(stockReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(stockReport));
    when(productService.getProductsByDate(date)).thenReturn(List.of());
    Stock stock2 = getStock2();
    stock2.setQuantity(I_TWELVE);
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(NO_RECORDS_FOR_THE_REPORT + D_STOCK_REPORT + date, thrown.getMessage());
  }

  @Test
  void
      testDailyReportsMethodWhenDailyStockCurrentDateAndNoReportExistsForTheDayThenReportPathIsReturned() {
    String date = getCurrentDate();
    ReportsRequest reportsRequest = getStockReportReq();
    reportsRequest.setDate(date);
    StockReport stockReport = getStockReport();
    when(stockReportRepository.findAll()).thenReturn(List.of(stockReport));
    when(svlcfService.getAvailableIdByName(STOCK_REPORT_STRING)).thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(stockReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(stockReport));
    when(productService.getProductsByDate(date)).thenReturn(getProductListWithDate(date));
    when(stockService.getStock()).thenReturn(getStockList());
    StockReport expectedStockReport = getExpectedStockReport();
    expectedStockReport.setDate(date);
    when(stockReportRepository.save(expectedStockReport)).thenReturn(stockReport);
    when(stockReportService.generateStockReport(
            date,
            getTheMapFromString(PREVIOUS_STOCK),
            getTheMapFromString(IN_STOCK_RECORDS),
            getTheMapFromString(OUT_STOCK_RECORDS),
            getTheMapFromString(GENERATED_CURRENT_STOCK)))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testDailyReportsMethodWhenDailyStockCurrentDateAndReportExistsForTheDayThenReportPathIsReturned() {
    String date = getCurrentDate();
    ReportsRequest reportsRequest = getStockReportReq();
    reportsRequest.setDate(date);
    StockReport stockReport = getStockReport();
    StockReport stockReport2 = getStockReport();
    stockReport2.setId(L_TEN_THOUSAND_AND_ONE);
    stockReport2.setDate(date);
    when(stockReportRepository.findAll()).thenReturn(List.of(stockReport, stockReport2));
    when(stockReportRepository.findByDate(date)).thenReturn(List.of(stockReport2));
    when(stockReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(stockReport));
    when(productService.getProductsByDate(date)).thenReturn(getProductListWithDate(date));
    when(stockService.getStock()).thenReturn(getStockList());
    StockReport expectedStockReport = getExpectedStockReport();
    expectedStockReport.setDate(date);
    when(stockReportRepository.save(expectedStockReport)).thenReturn(stockReport);
    when(stockReportService.generateStockReport(
            date,
            getTheMapFromString(PREVIOUS_STOCK),
            getTheMapFromString(IN_STOCK_RECORDS),
            getTheMapFromString(OUT_STOCK_RECORDS),
            getTheMapFromString(GENERATED_CURRENT_STOCK)))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testDailyReportsMethodWhenDailyStockOldDateAndReportExistsForTheDayThenReportPathIsReturned() {
    String date = PAST_REPORTS_DATE;
    ReportsRequest reportsRequest = getStockReportReq();
    reportsRequest.setDate(date);
    StockReport stockReport = getStockReport();
    when(stockReportRepository.findAll()).thenReturn(List.of(stockReport));
    when(svlcfService.getAvailableIdByName(STOCK_REPORT_STRING)).thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(stockReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(stockReport));
    when(productService.getProductsByDate(date)).thenReturn(getProductListWithDate(date));
    StockReport expectedStockReport = getExpectedStockReport();
    expectedStockReport.setDate(date);
    when(stockReportRepository.save(expectedStockReport)).thenReturn(stockReport);
    when(stockReportService.generateStockReport(
            date,
            getTheMapFromString(PREVIOUS_STOCK),
            getTheMapFromString(IN_STOCK_RECORDS),
            getTheMapFromString(OUT_STOCK_RECORDS),
            getTheMapFromString(GENERATED_CURRENT_STOCK)))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testDailyReportsMethodWhenDailyBalanceCurrentDateNoReportExistsForTheDayButFailedToGetPreviousReportThenExceptionIsThrown() {
    ReportsRequest reportsRequest = getBalanceReportReq();
    reportsRequest.setDate(getCurrentDate());
    when(balanceReportRepository.findAll()).thenReturn(List.of(getBalanceReport()));
    when(svlcfService.getAvailableIdByName(BALANCE_REPORT_STRING))
        .thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(balanceReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> reportsService.reports(reportsRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + BALANCE_REPORT_STRING, thrown.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {S_CAAC_EMPTY, S_CAAC_CA})
  void
      testDailyReportsMethodWhenDailyBalanceCurrentDateAndNoReportExistsForTheDayThenReportPathIsReturned(
          String caacType) {
    String date = getCurrentDate();
    ReportsRequest reportsRequest = getBalanceReportReq();
    reportsRequest.setDate(date);
    BalanceReport balanceReport = getBalanceReport();
    when(balanceReportRepository.findAll()).thenReturn(List.of(balanceReport));
    when(svlcfService.getAvailableIdByName(BALANCE_REPORT_STRING))
        .thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(balanceReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(balanceReport));
    when(paymentService.getPaymentForTheDate(date)).thenReturn(getPaymentList());
    when(userService.getAllUsers()).thenReturn(getUserListForBalanceReport());
    when(expenseService.getExpensesForTheDate(date)).thenReturn(List.of(getExpense()));
    BalanceReport expectedBalanceReport;
    if (caacType.equalsIgnoreCase(S_CAAC_EMPTY)) {
      expectedBalanceReport = getExpectedBalanceReport();
      when(cashDepositRepository.findById(date)).thenReturn(Optional.empty());
      when(balanceReportService.generateBalanceReport(
              getTheMapFromString(CAAC),
              List.of(
                  BalanceListWithDate.builder()
                      .inAmountRecords(getExpectedInAmountRecords())
                      .outAmountRecords(getExpectedOutAmountRecords())
                      .date(date)
                      .build())))
          .thenReturn(TEST_SAMPLE_PATH);
    } else {
      expectedBalanceReport = getExpectedBalanceReportCA();
      when(cashDepositRepository.findById(date)).thenReturn(Optional.of(getCashDeposit()));
      when(balanceReportService.generateBalanceReport(
              getTheMapFromString(CAAC_CA),
              List.of(
                  BalanceListWithDate.builder()
                      .inAmountRecords(getExpectedInAmountRecords())
                      .outAmountRecords(getExpectedOutAmountRecords())
                      .date(date)
                      .build())))
          .thenReturn(TEST_SAMPLE_PATH);
    }
    expectedBalanceReport.setDate(date);
    when(balanceReportRepository.save(expectedBalanceReport)).thenReturn(balanceReport);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testDailyReportsMethodWhenDailyBalanceCurrentDateAndReportExistsForTheDayThenReportPathIsReturned() {
    String date = getCurrentDate();
    ReportsRequest reportsRequest = getBalanceReportReq();
    reportsRequest.setDate(date);
    BalanceReport balanceReport = getBalanceReport();
    BalanceReport balanceReport2 = getBalanceReport();
    balanceReport2.setId(L_TEN_THOUSAND_AND_ONE);
    balanceReport2.setDate(date);
    when(balanceReportRepository.findAll()).thenReturn(List.of(balanceReport, balanceReport2));
    when(balanceReportRepository.findByDate(date)).thenReturn(List.of(balanceReport2));
    when(balanceReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(balanceReport));
    when(paymentService.getPaymentForTheDate(date)).thenReturn(getPaymentList());
    when(userService.getAllUsers()).thenReturn(getUserListForBalanceReport());
    when(expenseService.getExpensesForTheDate(date)).thenReturn(List.of(getExpense()));
    when(cashDepositRepository.findById(date)).thenReturn(Optional.empty());
    BalanceReport expectedBalanceReport = getExpectedBalanceReport();
    expectedBalanceReport.setDate(date);
    when(balanceReportRepository.save(expectedBalanceReport)).thenReturn(balanceReport);
    when(balanceReportService.generateBalanceReport(
            getTheMapFromString(CAAC),
            List.of(
                BalanceListWithDate.builder()
                    .inAmountRecords(getExpectedInAmountRecords())
                    .outAmountRecords(getExpectedOutAmountRecords())
                    .date(date)
                    .build())))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testDailyReportsMethodWhenDailyBalanceOldDateAndNoReportExistsForTheDayThenReportPathIsReturned() {
    String date = PAST_REPORTS_DATE;
    ReportsRequest reportsRequest = getBalanceReportReq();
    reportsRequest.setDate(date);
    BalanceReport balanceReport = getBalanceReport();
    when(balanceReportRepository.findAll()).thenReturn(List.of(balanceReport));
    when(svlcfService.getAvailableIdByName(BALANCE_REPORT_STRING))
        .thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(balanceReportRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(balanceReport));
    when(paymentService.getPaymentForTheDate(date)).thenReturn(getPaymentList());
    when(userService.getAllUsers()).thenReturn(getUserListForBalanceReport());
    when(expenseService.getExpensesForTheDate(date)).thenReturn(List.of(getExpense()));
    when(cashDepositRepository.findById(date)).thenReturn(Optional.empty());
    BalanceReport expectedBalanceReport = getExpectedBalanceReport();
    expectedBalanceReport.setDate(date);
    when(balanceReportRepository.save(expectedBalanceReport)).thenReturn(balanceReport);
    when(balanceReportService.generateBalanceReport(
            getTheMapFromString(CAAC),
            List.of(
                BalanceListWithDate.builder()
                    .inAmountRecords(getExpectedInAmountRecords())
                    .outAmountRecords(getExpectedOutAmountRecords())
                    .date(date)
                    .build())))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void testDailyReportsMethodWhenMonthlyBalanceRequestedThenReportPathIsReturned() {
    ReportsRequest reportsRequest = getMonthlyBalanceReportReq();
    Payment payment = getPayment();
    payment.setDate(PAST_REPORTS_DATE);
    Payment payment2 = getPayment2();
    payment2.setDate(PAST_REPORTS_DATE);
    when(paymentService.getPayments()).thenReturn(List.of(payment, payment2));
    when(userService.getAllUsers()).thenReturn(getUserListForBalanceReport());
    when(balanceReportService.generateBalanceReport(any(), any())).thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void
      testGetUserReportMethodWhenValidConsumerIsSentWithIsPartOfMultipleCallsFalseThenValidReportIsGenerated() {
    User user = getConsumerWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_ONE)).thenReturn(user);
    Product product = getProduct();
    Product product2 = getProduct2();
    when(productService.getProductByUserId(L_TEN_THOUSAND_AND_ONE))
        .thenReturn(List.of(product, product2));
    List<Payment> payments = List.of(getPayment(), getPayment2());
    when(paymentService.getPaymentRecordsByUserId(L_TEN_THOUSAND_AND_ONE)).thenReturn(payments);
    when(preBalanceRepository.findById(L_TEN_THOUSAND_AND_ONE)).thenReturn(Optional.empty());
    when(userReportService.generateUserReport(
            user, List.of(product, product2), payments, null, false))
        .thenReturn(TEST_SAMPLE_PATH);
    assertEquals(TEST_SAMPLE_PATH, reportsService.getUserReport(L_TEN_THOUSAND_AND_ONE, false));
  }

  @Test
  void
      testGetUserReportMethodWhenValidSupplierIsSentWithIsPartOfMultipleCallsFalseThenValidReportIsGenerated() {
    User user = getSupplierWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(user);
    when(paymentService.getPaymentRecordsByUserId(L_TEN_THOUSAND_AND_THREE)).thenReturn(List.of());
    when(preBalanceRepository.findById(L_TEN_THOUSAND_AND_THREE)).thenReturn(Optional.empty());
    when(userReportService.generateUserReport(user, List.of(), List.of(), null, false))
        .thenReturn(TEST_SAMPLE_PATH);
    assertEquals(TEST_SAMPLE_PATH, reportsService.getUserReport(L_TEN_THOUSAND_AND_THREE, false));
  }

  @Test
  void
      testGetAllUserReportsWhenValidSupplierWithIsSentWithIsPartOfMultipleCallsThenValidReportIsGenerated() {
    User user = getSupplierWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND_AND_THREE)).thenReturn(user);
    when(paymentService.getPaymentRecordsByUserId(L_TEN_THOUSAND_AND_THREE)).thenReturn(List.of());
    when(preBalanceRepository.findById(L_TEN_THOUSAND_AND_THREE)).thenReturn(Optional.empty());
    when(userReportService.generateUserReport(user, List.of(), List.of(), null, true))
        .thenReturn(TEST_SAMPLE_PATH);
    when(userService.getAllUsers()).thenReturn(List.of(user));
    assertEquals(SUCCESS, reportsService.getAllUserReports());
  }

  @Test
  void testDailyReportsMethodWhenDailySalesThenReportPathIsReturned() {
    ReportsRequest reportsRequest = getSalesReportReq();
    when(salesService.getSalesReportForTheDate(reportsRequest.getDate()))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void testDailyReportsMethodWhenRequestedForMonthlySalesReportThenReportPathIsReturned() {
    ReportsRequest reportsRequest = getMonthlySalesReportReq();
    when(salesService.getSalesReportForTheMonth(any())).thenReturn(TEST_SAMPLE_PATH);
    String path = reportsService.reports(reportsRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  private List<Stock> getStockList() {
    return List.of(getStock(), getStock2(), getStock4());
  }

  private List<Product> getProductListWithDate(String date) {
    return Stream.of(getProduct(), getProduct2(), getProduct4(), getProduct5())
        .peek(product -> product.setDate(date))
        .toList();
  }

  private List<Payment> getPaymentList() {
    return List.of(getPayment(), getPayment2(), getPayment3(), getPayment4());
  }

  private List<User> getUserListForBalanceReport() {
    return List.of(getConsumerWithBalance(), getSupplierWithBalance());
  }
}
