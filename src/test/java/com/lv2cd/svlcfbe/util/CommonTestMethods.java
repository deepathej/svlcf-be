package com.lv2cd.svlcfbe.util;

import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lv2cd.svlcfbe.entity.*;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class CommonTestMethods {

  private CommonTestMethods() {}

  public static <T> T asObjectFromString(final String string, Class<T> valueType) {
    try {
      return new ObjectMapper().readValue(string, valueType);
    } catch (Exception e) {
      throw new CustomInternalServerException(e.getMessage());
    }
  }

  public static <T> T readJsonFile(String fileName, Class<T> valueType) {
    String jsonString;
    Resource resource = new ClassPathResource(fileName);
    try (InputStream inputStream = resource.getInputStream()) {
      byte[] fileDataBytes = new byte[inputStream.available()];
      inputStream.read(fileDataBytes);
      jsonString = new String(fileDataBytes, StandardCharsets.UTF_8);
      return new ObjectMapper().readValue(jsonString, valueType);
    } catch (IOException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
  }

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new CustomInternalServerException(e.getMessage());
    }
  }

  public static Map<String, Integer> getTheMapFromString(String data) {
    return Arrays.stream(data.split(COMMA))
        .map(mapEntry -> mapEntry.split(COLON_SPACE.trim()))
        .collect(Collectors.toMap(entry -> entry[I_ZERO], entry -> Integer.parseInt(entry[I_ONE])));
  }

  public static List<BalanceStmtRecord> getExpectedInAmountRecords() {
    return List.of(getInBalanceStmtRecord(), getInBalanceStmtRecord2());
  }

  public static List<BalanceStmtRecord> getExpectedOutAmountRecords() {
    return List.of(
        getOutBalanceStmtRecord(), getOutBalanceStmtRecord2(), getOutBalanceStmtRecord3());
  }

  public static void validateAndDeleteFile(String filePath) {
    File file = new File(filePath);
    if (file.delete()) {
      System.out.println(TEST_REPORT_FILE_DELETED);
    }
  }

  public static Map<String, String> getPreBalanceMap() {
    Map<String, String> userDetailsAndPreBalanceMap = new HashMap<>();
    User user = getConsumerWithBalance();
    userDetailsAndPreBalanceMap.put(
        user.getId() + UNDER_SCORE + user.getName(),
        getConsumerWithPreBalance().getBalanceAmount().toString());
    User user1 = getSupplierWithBalance();
    userDetailsAndPreBalanceMap.put(
        user1.getId() + UNDER_SCORE + user1.getName(),
        getSupplierWithPreBalance().getBalanceAmount().toString());
    return userDetailsAndPreBalanceMap;
  }

  public static User getConsumerWithBalance() {
    return readJsonFile("/mocks/Consumer_Balance.json", User.class);
  }

  public static User getConsumerWithBalanceReq() {
    return readJsonFile("/mocks/Consumer_Balance_Req.json", User.class);
  }

  public static User getConsumerWithZeroBalance() {
    return readJsonFile("/mocks/Consumer_Zero_Balance.json", User.class);
  }

  public static User getConsumerWithZeroBalanceReq() {
    return readJsonFile("/mocks/Consumer_Zero_Balance_Req.json", User.class);
  }

  public static User getConsumerUpdateReq() {
    return readJsonFile("/mocks/Consumer_Update_Req.json", User.class);
  }

  public static User getConsumerUpdateEmptyReq() {
    return readJsonFile("/mocks/Consumer_Update_Empty_Req.json", User.class);
  }

  public static User getConsumerUpdateNullReq() {
    return readJsonFile("/mocks/Consumer_Update_Null_Req.json", User.class);
  }

  public static User getSupplierWithBalance() {
    return readJsonFile("/mocks/Supplier_Balance.json", User.class);
  }

  public static User getSupplierWithBalanceReq() {
    return readJsonFile("/mocks/Supplier_Balance_Req.json", User.class);
  }

  public static User getSupplierWithZeroBalance() {
    return readJsonFile("/mocks/Supplier_Zero_Balance.json", User.class);
  }

  public static User getSupplierWithZeroBalanceReq() {
    return readJsonFile("/mocks/Supplier_Zero_Balance_Req.json", User.class);
  }

  public static PreBalance getConsumerWithPreBalance() {
    return readJsonFile("/mocks/Consumer_PreBalance.json", PreBalance.class);
  }

  public static PreBalance getSupplierWithPreBalance() {
    return readJsonFile("/mocks/Supplier_PreBalance.json", PreBalance.class);
  }

  private static BalanceStmtRecord getInBalanceStmtRecord() {
    return readJsonFile("/mocks/InBalanceStmtRecord.json", BalanceStmtRecord.class);
  }

  private static BalanceStmtRecord getInBalanceStmtRecord2() {
    return readJsonFile("/mocks/InBalanceStmtRecord2.json", BalanceStmtRecord.class);
  }

  private static BalanceStmtRecord getOutBalanceStmtRecord() {
    return readJsonFile("/mocks/OutBalanceStmtRecord.json", BalanceStmtRecord.class);
  }

  private static BalanceStmtRecord getOutBalanceStmtRecord2() {
    return readJsonFile("/mocks/OutBalanceStmtRecord2.json", BalanceStmtRecord.class);
  }

  private static BalanceStmtRecord getOutBalanceStmtRecord3() {
    return readJsonFile("/mocks/OutBalanceStmtRecord3.json", BalanceStmtRecord.class);
  }

  public static Payment getPaymentReq() {
    return readJsonFile("/mocks/Payment_Req.json", Payment.class);
  }

  public static Payment getPayment() {
    return readJsonFile("/mocks/Payment.json", Payment.class);
  }

  public static Payment getPayment2() {
    return readJsonFile("/mocks/Payment2.json", Payment.class);
  }

  public static Payment getPayment3() {
    return readJsonFile("/mocks/Payment3.json", Payment.class);
  }

  public static Payment getPayment4() {
    return readJsonFile("/mocks/Payment4.json", Payment.class);
  }

  public static Product getProduct() {
    return readJsonFile("/mocks/Product.json", Product.class);
  }

  public static Product getProduct2() {
    return readJsonFile("/mocks/Product2.json", Product.class);
  }

  public static Product getProduct3() {
    return readJsonFile("/mocks/Product3.json", Product.class);
  }

  public static Product getProduct4() {
    return readJsonFile("/mocks/Product4.json", Product.class);
  }

  public static Product getProduct5() {
    return readJsonFile("/mocks/Product5.json", Product.class);
  }

  public static Svlcf getSvlcf() {
    return readJsonFile("/mocks/Svlcf.json", Svlcf.class);
  }

  public static Expense getExpenseReq() {
    return readJsonFile("/mocks/Expense_Req.json", Expense.class);
  }

  public static DataHealthGraph getDataHealthGraph() {
    return DataHealthGraph.builder()
        .userData(GREEN)
        .dataBaseBackup(RED)
        .stockReport(GREEN)
        .build();
  }

  public static Expense getExpense() {
    return readJsonFile("/mocks/Expense.json", Expense.class);
  }

  public static SalesRequest getConsumerSaleReq() {
    return readJsonFile("/mocks/Consumer_Sale_Req.json", SalesRequest.class);
  }

  public static SalesRequest getConsumerSaleReqWithEmptyProducts() {
    return readJsonFile("/mocks/Empty_Products_Sale_Req.json", SalesRequest.class);
  }

  public static SalesRequest getSupplierSaleReq() {
    return readJsonFile("/mocks/Supplier_Sale_Req.json", SalesRequest.class);
  }

  public static StockRequest getConsumerStockReq() {
    return readJsonFile("/mocks/Consumer_Stock_Req.json", StockRequest.class);
  }

  public static StockRequest getSupplierStockReq() {
    return readJsonFile("/mocks/Supplier_Stock_Req.json", StockRequest.class);
  }

  public static Stock getStock() {
    return readJsonFile("/mocks/Stock.json", Stock.class);
  }

  public static Stock getStock2() {
    return readJsonFile("/mocks/Stock2.json", Stock.class);
  }

  public static Stock getStock3() {
    return readJsonFile("/mocks/Stock3.json", Stock.class);
  }

  public static Stock getStock4() {
    return readJsonFile("/mocks/Stock4.json", Stock.class);
  }

  public static Stock getUpdateStockQuantity() {
    return readJsonFile("/mocks/Update_Stock_Quantity.json", Stock.class);
  }

  public static OldStock getOldStock() {
    return readJsonFile("/mocks/Old_Stock.json", OldStock.class);
  }

  public static Stock getOldStockReq() {
    return readJsonFile("/mocks/Old_Stock_Req.json", Stock.class);
  }

  public static SaleDetailForDay getSaleDetailForDay() {
    return readJsonFile("/mocks/Expected_sales.json", SaleDetailForDay.class);
  }

  public static Sales getSales() {
    return readJsonFile("/mocks/Sales.json", Sales.class);
  }

  public static DelTrans getDelTrans() {
    return readJsonFile("/mocks/DelTrans.json", DelTrans.class);
  }

  public static CashDeposit getCashDeposit() {
    return readJsonFile("/mocks/CashDeposit.json", CashDeposit.class);
  }

  public static BalanceReport getExpectedBalanceReport() {
    return readJsonFile("/mocks/Expected_Balance_Report.json", BalanceReport.class);
  }

  public static BalanceReport getExpectedBalanceReportAC() {
    return readJsonFile("/mocks/Expected_Balance_Report_AC.json", BalanceReport.class);
  }

  public static BalanceReport getExpectedBalanceReportCA() {
    return readJsonFile("/mocks/Expected_Balance_Report_CA.json", BalanceReport.class);
  }

  public static ReportsRequest getReportsRequestWithEmptyDate() {
    return readJsonFile("/mocks/EmptyDate_Report_Req.json", ReportsRequest.class);
  }

  public static ReportsRequest getReportsRequestWithInvalidType() {
    return readJsonFile("/mocks/InvalidType_Report_Req.json", ReportsRequest.class);
  }

  public static BalanceReport getBalanceReport() {
    return readJsonFile("/mocks/Balance_Report.json", BalanceReport.class);
  }

  public static ReportsRequest getBalanceReportReq() {
    return readJsonFile("/mocks/Balance_Report_Req.json", ReportsRequest.class);
  }

  public static ReportsRequest getMonthlyBalanceReportReq() {
    return readJsonFile("/mocks/Monthly_Balance_Report_Req.json", ReportsRequest.class);
  }

  public static ReportsRequest getSalesReportReq() {
    return readJsonFile("/mocks/Sales_Report_Req.json", ReportsRequest.class);
  }

  public static ReportsRequest getMonthlySalesReportReq() {
    return readJsonFile("/mocks/Monthly_Sales_Report_Req.json", ReportsRequest.class);
  }

  public static StockReport getStockReport() {
    return readJsonFile("/mocks/Stock_Report.json", StockReport.class);
  }

  public static ReportsRequest getStockReportReq() {
    return readJsonFile("/mocks/Stock_Report_Req.json", ReportsRequest.class);
  }

  public static StockReport getExpectedStockReport() {
    return readJsonFile("/mocks/Expected_Stock_Report.json", StockReport.class);
  }
}
