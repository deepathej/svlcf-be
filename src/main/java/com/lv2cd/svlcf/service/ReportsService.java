package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.enums.PaymentMode.*;
import static com.lv2cd.svlcf.enums.UserType.CONSUMER;
import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.CommonPdfMethods.makeZeroIfNull;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.config.SvlcfConfig;
import com.lv2cd.svlcf.entity.*;
import com.lv2cd.svlcf.enums.PaymentMode;
import com.lv2cd.svlcf.enums.UserType;
import com.lv2cd.svlcf.exception.CustomBadRequestException;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import com.lv2cd.svlcf.model.BalanceListWithDate;
import com.lv2cd.svlcf.model.BalanceStmtRecord;
import com.lv2cd.svlcf.model.ReportsRequest;
import com.lv2cd.svlcf.repository.BalanceReportRepository;
import com.lv2cd.svlcf.repository.CashDepositRepository;
import com.lv2cd.svlcf.repository.PreBalanceRepository;
import com.lv2cd.svlcf.repository.StockReportRepository;
import com.lv2cd.svlcf.service.generatepdf.BalanceReportService;
import com.lv2cd.svlcf.service.generatepdf.StockReportService;
import com.lv2cd.svlcf.service.generatepdf.UserReportService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ReportsService {

  private ExpenseService expenseService;
  private PaymentService paymentService;
  private UserService userService;
  private ProductService productService;
  private StockService stockService;
  private BalanceReportService balanceReportService;
  private StockReportService stockReportService;
  private BalanceReportRepository balanceReportRepository;
  private StockReportRepository stockReportRepository;
  private SvlcfService svlcfService;
  private SalesService salesService;
  private PreBalanceRepository preBalanceRepository;
  private UserReportService userReportService;
  private CashDepositRepository cashDepositRepository;
  private SvlcfConfig svlcfConfig;

  private static int getSumOfAmountRecords(
      List<BalanceStmtRecord> amountRecords, PaymentMode paymentMode) {
    return amountRecords.stream()
        .filter(
            balanceStmtRecord ->
                balanceStmtRecord.getPaymentMode() != DISCOUNT
                    && ((paymentMode == CASH) == (balanceStmtRecord.getPaymentMode() == CASH)))
        .mapToInt(BalanceStmtRecord::getAmount)
        .sum();
  }

  private static Map<String, Integer> getStockRecords(
      String date, List<Product> productList, Long invoiceNumber) {
    return productList.stream()
        .filter(
            product -> {
              if (invoiceNumber.equals(L_STOCK_INVOICE_NUMBER)) {
                return product.getDate().equals(date)
                    && product.getInvoiceNumber().equals(L_STOCK_INVOICE_NUMBER);
              } else {
                return product.getDate().equals(date)
                    && !product.getInvoiceNumber().equals(L_STOCK_INVOICE_NUMBER);
              }
            })
        .collect(
            Collectors.toMap(
                product -> product.getItem() + UNDER_SCORE + product.getVariant(),
                Product::getQuantity,
                Integer::sum));
  }

  private static void updateTotalStock(Map<String, Integer> stockMap) {
    stockMap.put(TOTAL_STOCK, stockMap.values().stream().reduce(0, Integer::sum));
  }

  public String reports(ReportsRequest reportsRequest) {
    String date = reportsRequest.getDate();
    if (date == null || date.trim().isEmpty()) {
      throw new CustomBadRequestException(PLEASE_SELECT_DATE);
    }
    return switch (reportsRequest.getType()) {
      case DAILY_STOCK_REPORT -> dailyStockReport(date);
      case DAILY_BALANCE_REPORT -> dailyBalanceReport(date);
      case DAILY_SALES_REPORT -> salesService.getSalesReportForTheDate(date);
      case MONTHLY_SALES_REPORT ->
          salesService.getSalesReportForTheMonth(getAllDatesForTheMonth(date));
      case MONTHLY_BALANCE_REPORT -> monthlyBalanceReport(getAllDatesForTheMonth(date));
      default -> throw new CustomBadRequestException(PLEASE_SELECT_TYPE);
    };
  }

  private Stream<String> getAllDatesForTheMonth(String date) {
    String[] yearAndMonth = date.split(HYPHEN);
    YearMonth ym =
        YearMonth.of(
            Integer.parseInt(yearAndMonth[I_ZERO]),
            Month.of(Integer.parseInt(yearAndMonth[I_ONE])));
    return ym.atDay(I_ONE)
        .datesUntil(ym.plusMonths(I_ONE).atDay(I_ONE))
        .map(singleDate -> singleDate.format(DateTimeFormatter.ofPattern(DATE_FORMAT)));
  }

  public String dailyStockReport(String date) {
    List<String> dates =
        stockReportRepository.findAll().stream().map(StockReport::getDate).toList();
    Long availId = isReportAvailableInDBForTheDate(dates, date, STOCK_REPORT_STRING);
    Map<String, Integer> previousStock =
        getIndividualStockDetailsFromLastStockReport(availId, null);
    List<Product> productList = productService.getProductsByDate(date);
    Map<String, Integer> outStockRecords = getStockRecords(date, productList, L_ONE);
    Map<String, Integer> inStockRecords =
        getStockRecords(date, productList, L_STOCK_INVOICE_NUMBER);
    if (isStockEmpty(inStockRecords) && isStockEmpty(outStockRecords)) {
      svlcfService.updateAvailIdByName(STOCK_REPORT_STRING, availId);
      throw new CustomBadRequestException(NO_RECORDS_FOR_THE_REPORT + D_STOCK_REPORT + date);
    }
    Map<String, Integer> generatedCurrentStock =
        generateCurrentStock(previousStock, outStockRecords, inStockRecords);
    if (date.equalsIgnoreCase(getCurrentDate())) {
      validateTheStockChanges(generatedCurrentStock, null, true);
    }
    stockReportRepository.save(
        new StockReport(
            availId,
            date,
            generatedCurrentStock.entrySet().stream().map(Object::toString).toList().toString()));
    updateTotalStock(previousStock);
    updateTotalStock(inStockRecords);
    updateTotalStock(outStockRecords);
    updateTotalStock(generatedCurrentStock);
    return stockReportService.generateStockReport(
        date, previousStock, inStockRecords, outStockRecords, generatedCurrentStock);
  }

  private boolean isStockEmpty(Map<String, Integer> stockRecords) {
    return stockRecords.values().stream().mapToInt(Integer::intValue).sum() == 0;
  }

  private Map<String, Integer> getIndividualStockDetailsFromLastStockReport(
      Long availId, StockReport stockReport) {
    return getLastStockReportDetails(availId, stockReport).stream()
        .map(entry -> entry.trim().split(EQUALSYMBOL))
        .collect(
            Collectors.toMap(
                entry -> entry[I_ZERO].trim(), entry -> Integer.parseInt(entry[I_ONE])));
  }

  private List<String> getLastStockReportDetails(Long availId, StockReport stockReport) {
    return Arrays.asList(
        getLastStockDetails(availId, stockReport)
            .getCurrentStock()
            .replace(OPEN_RECT_BRACES, BLANK_STRING)
            .replace(CLOSED_RECT_BRACES, BLANK_STRING)
            .split(COMMA));
  }

  private StockReport getLastStockDetails(Long availId, StockReport stockReport) {
    return availId != null
        ? stockReportRepository
            .findById(availId - L_ONE)
            .orElseThrow(
                () -> new CustomInternalServerException(FAILED_DB_OPERATION + STOCK_REPORT_STRING))
        : stockReport;
  }

  private void validateTheStockChanges(
      Map<String, Integer> generatedCurrentStock,
      Map<String, Integer> currentStock,
      boolean updateId) {
    if (currentStock == null) {
      currentStock = getCurrentStockMap();
    }
    if (!currentStock.equals(generatedCurrentStock)) {
      if (updateId) {
        svlcfService.updateAvailIdByName(
            STOCK_REPORT_STRING, svlcfService.getOnlyAvailableIdByName(STOCK_REPORT_STRING) - 1);
      }
      logMismatchedStock(currentStock, generatedCurrentStock);
      throw new CustomInternalServerException(DB_DATA_ISSUE + STOCK_MISMATCH);
    }
  }

  private void logMismatchedStock(
      Map<String, Integer> currentStock, Map<String, Integer> generatedCurrentStock) {
    currentStock.forEach(
        (item, value) -> {
          if (!value.equals(generatedCurrentStock.get(item))) {
            log.error(
                LOG_DATA_REPLACER
                    + HYPHEN_WITH_SPACE
                    + LOG_DATA_REPLACER
                    + HYPHEN_WITH_SPACE
                    + LOG_DATA_REPLACER,
                item,
                value,
                generatedCurrentStock.get(item));
          }
        });
  }

  private Map<String, Integer> generateCurrentStock(
      Map<String, Integer> previousStock,
      Map<String, Integer> outStockRecords,
      Map<String, Integer> inStockRecords) {
    Map<String, Integer> currentStock =
        previousStock.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry ->
                        entry.getValue()
                            + makeZeroIfNull(inStockRecords.get(entry.getKey()))
                            - makeZeroIfNull(outStockRecords.get(entry.getKey()))));
    inStockRecords.forEach(
        (key, value) -> {
          if (!currentStock.containsKey(key)) {
            currentStock.put(key, value - makeZeroIfNull(outStockRecords.get(key)));
          }
        });
    return currentStock;
  }

  private Map<String, Integer> getCurrentStockMap() {
    return stockService.getStock().stream()
        .collect(
            Collectors.toMap(
                stock ->
                    stock.getBrand()
                        + UNDER_SCORE
                        + stock.getName()
                        + UNDER_SCORE
                        + stock.getVariant(),
                Stock::getQuantity));
  }

  public String monthlyBalanceReport(Stream<String> dates) {
    List<Payment> payments = paymentService.getPayments();
    List<User> userList = userService.getAllUsers();
    List<BalanceListWithDate> balanceListWithDateList =
        dates
            .parallel()
            .map(
                date -> {
                  List<Payment> dailyPayment =
                      payments.stream()
                          .filter(payment -> payment.getDate().equalsIgnoreCase(date))
                          .toList();
                  return BalanceListWithDate.builder()
                      .inAmountRecords(getBalanceStmtInAmountRecords(dailyPayment, userList))
                      .outAmountRecords(
                          getBalanceStmtOutAmountRecords(date, dailyPayment, userList))
                      .date(date)
                      .build();
                })
            .toList();
    return balanceReportService.generateBalanceReport(new HashMap<>(), balanceListWithDateList);
  }

  public String dailyBalanceReport(String date) {
    List<String> dates =
        balanceReportRepository.findAll().stream().map(BalanceReport::getDate).toList();
    Long availId = isReportAvailableInDBForTheDate(dates, date, BALANCE_REPORT_STRING);
    BalanceReport balanceReport =
        balanceReportRepository
            .findById(availId - L_ONE)
            .orElseThrow(
                () ->
                    new CustomInternalServerException(FAILED_DB_OPERATION + BALANCE_REPORT_STRING));
    Map<String, Integer> balanceMap = new HashMap<>();
    balanceMap.put(PREVIOUS_ACCOUNT_BALANCE, balanceReport.getAccountBalance());
    balanceMap.put(PREVIOUS_CASH_BALANCE, balanceReport.getCashBalance());
    List<Payment> dailyPayment = paymentService.getPaymentForTheDate(date);
    List<User> userList = userService.getAllUsers();
    List<BalanceStmtRecord> outAmountRecords =
        getBalanceStmtOutAmountRecords(date, dailyPayment, userList);
    List<BalanceStmtRecord> inAmountRecords = getBalanceStmtInAmountRecords(dailyPayment, userList);
    int cashDeposited = getCashDepositedAmount(date);
    int accountBalance =
        balanceMap.get(PREVIOUS_ACCOUNT_BALANCE)
            + getSumOfAmountRecords(inAmountRecords, ACCOUNT)
            + cashDeposited
            - getSumOfAmountRecords(outAmountRecords, ACCOUNT);
    int cashBalance =
        balanceMap.get(PREVIOUS_CASH_BALANCE)
            + getSumOfAmountRecords(inAmountRecords, CASH)
            - cashDeposited
            - getSumOfAmountRecords(outAmountRecords, CASH);
    balanceMap.put(ACCOUNT_BALANCE, accountBalance);
    balanceMap.put(CASH_BALANCE, cashBalance);
    balanceReportRepository.save(new BalanceReport(availId, date, accountBalance, cashBalance));
    return balanceReportService.generateBalanceReport(
        balanceMap,
        List.of(
            BalanceListWithDate.builder()
                .inAmountRecords(inAmountRecords)
                .outAmountRecords(outAmountRecords)
                .date(date)
                .build()));
  }

  private int getCashDepositedAmount(String date) {
    return cashDepositRepository.findById(date).map(CashDeposit::getCash).orElse(0);
  }

  private List<BalanceStmtRecord> getBalanceStmtInAmountRecords(
      List<Payment> dailyPayment, List<User> userList) {
    return dailyPayment.stream()
        .filter(payment -> payment.getUserType() == UserType.CONSUMER)
        .map(payment -> getBalanceStmtRecord(userList, payment))
        .toList();
  }

  private BalanceStmtRecord getBalanceStmtRecord(List<User> userList, Payment payment) {
    User user =
        userList.stream()
            .filter(user1 -> user1.getId().equals(payment.getUserId()))
            .findFirst()
            .orElseThrow(
                () -> new CustomInternalServerException(FAILED_DB_OPERATION + USER_STRING));
    return new BalanceStmtRecord(
        user.getId(), user.getName(), payment.getPaymentMode(), payment.getPaymentAmount());
  }

  private List<BalanceStmtRecord> getBalanceStmtOutAmountRecords(
      String date, List<Payment> dailyPayment, List<User> userList) {
    return Stream.concat(
            expenseService.getExpensesForTheDate(date).stream()
                .map(
                    expense ->
                        new BalanceStmtRecord(
                            null,
                            expense.getRemarks(),
                            expense.getPaymentMode(),
                            expense.getAmount())),
            dailyPayment.stream()
                .filter(payment -> payment.getUserType() == UserType.SUPPLIER)
                .map(payment -> getBalanceStmtRecord(userList, payment)))
        .toList();
  }

  private Long isReportAvailableInDBForTheDate(List<String> dates, String date, String reportType) {
    if (!dates.contains(date)) {
      return svlcfService.getAvailableIdByName(reportType);
    } else if (getCurrentDate().equalsIgnoreCase(date)) {
      return getLastReportId(reportType, date);
    } else {
      throw new CustomBadRequestException(DUPLICATE_REPORT_FOR_PAST_DATES);
    }
  }

  private Long getLastReportId(String reportType, String date) {
    return reportType.equalsIgnoreCase(BALANCE_REPORT_STRING)
        ? balanceReportRepository.findByDate(date).get(0).getId()
        : stockReportRepository.findByDate(date).get(0).getId();
  }

  public String getUserReport(Long userId, boolean isPartOfMultipleCalls) {
    User user = userService.getUserById(userId);
    List<Product> productList =
        user.getUserType() == CONSUMER
            ? productService.getProductByUserId(userId).stream()
                .filter(product -> !product.getInvoiceNumber().equals(L_STOCK_INVOICE_NUMBER))
                .toList()
            : productService.getProductByUserId(userId).stream()
                .filter(product -> product.getInvoiceNumber().equals(L_STOCK_INVOICE_NUMBER))
                .toList();
    return userReportService.generateUserReport(
        user,
        productList,
        paymentService.getPaymentRecordsByUserId(userId),
        preBalanceRepository.findById(userId).orElse(null),
        isPartOfMultipleCalls);
  }

  public String getAllUserReports() {
    userService.getAllUsers().parallelStream()
        .map(User::getId)
        .forEach(id -> getUserReport(id, true));
    return SUCCESS;
  }

  public String validateStockReports() {
    List<StockReport> stockReports =
        stockReportRepository.findAll().stream()
            .sorted(Comparator.comparing(StockReport::getId))
            .toList();
    StockReport firstStockReport;
    StockReport secondStockReport;
    for (int stockId = I_ONE; stockId < stockReports.size(); stockId++) {
      firstStockReport = stockReports.get(stockId - I_ONE);
      secondStockReport = stockReports.get(stockId);
      Map<String, Integer> firstStock =
          getIndividualStockDetailsFromLastStockReport(null, firstStockReport);
      Map<String, Integer> secondStock =
          getIndividualStockDetailsFromLastStockReport(null, secondStockReport);
      log.info(
          LOG_DATA_REPLACER + EMPTY_STRING + LOG_DATA_REPLACER + EMPTY_STRING + LOG_DATA_REPLACER,
          firstStockReport.getDate(),
          HYPHEN_WITH_SPACE,
          secondStockReport.getDate());
      List<Product> productList = productService.getProductsByDate(secondStockReport.getDate());
      Map<String, Integer> outStockRecords =
          getStockRecords(secondStockReport.getDate(), productList, L_ONE);
      Map<String, Integer> inStockRecords =
          getStockRecords(secondStockReport.getDate(), productList, L_STOCK_INVOICE_NUMBER);
      Map<String, Integer> generatedCurrentStock =
          generateCurrentStock(firstStock, outStockRecords, inStockRecords);
      validateTheStockChanges(generatedCurrentStock, secondStock, false);
    }
    return SUCCESS;
  }

  public String createDirectories() {
    String rootPath = svlcfConfig.getPdfOutputRootPath();
    List<String> directories =
        List.of(
            "Balance",
            "DbBackUpData",
            "Duplicate",
            "Invoices",
            "Logs",
            "Sales",
            "Stock",
            "Support",
            "UserReport",
            "UnitTest");
    checkAndCreateDir(directories, rootPath);
    checkAndCreateDir(directories, rootPath + "UnitTest/");
    return SUCCESS;
  }

  private static void checkAndCreateDir(List<String> directories, String rootPath) {
    log.info("Create the required directories if not available");
    directories.forEach(
        directory -> {
          Path dirPath = Path.of(rootPath).resolve(directory);
          if (Files.notExists(dirPath)) {
            try {
              Files.createDirectories(dirPath);
              log.info("Created path {}", dirPath);
            } catch (IOException e) {
              log.error("Failed to create path {}", dirPath);
            }
          } else {
            log.info("Already existed path {}", dirPath);
          }
        });
  }
}
