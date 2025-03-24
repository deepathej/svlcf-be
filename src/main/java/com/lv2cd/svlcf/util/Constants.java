package com.lv2cd.svlcf.util;

public class Constants {

  // Exception Messages
  public static final String STOCK_UPDATE_INSTEAD = "Use addStock instead of newStock";
  public static final String PRICE_CANNOT_BE_NULL = "price cannot be null/zero while updating : ";
  public static final String PRICE_UPDATE_FAILED = "Failed to update price";
  public static final String USER_ALREADY_EXISTS = "User already exists in DB";
  public static final String SEQUENCE_VALUE_UPDATE_FAILED =
      "Sequence value update to DB failed for ";
  public static final String BALANCE_UPDATE_FAILED = "Failed to update the Balance: ";
  public static final String ONLY_USER_LAST_PAYMENT_CAN_BE_DELETED =
      "Only last payment of a user can be deleted";
  public static final String CONSUMER_CANNOT_BE_DELETED =
      "Consumer cannot be deleted before paying the balance : ";
  public static final String SUPPLIER_CANNOT_BE_DELETED =
      "Supplier cannot be deleted before settling the balance : ";
  public static final String USER_NOT_REGISTERED = "User not registered";
  public static final String PRODUCT_NOT_AVAILABLE = "Product not Available";
  public static final String USER_CANNOT_ADD_OR_UPDATE_STOCK =
      "Consumer and cannot add/update stock";
  public static final String CANNOT_SALE_TO_SUPPLIER = "Sale cannot be done with supplier";
  public static final String QUANTITY_CANNOT_BE_NULL =
      "quantity cannot be null/zero while updating : ";
  public static final String STOCK_NOT_EXIST = "stock does not exists";
  public static final String QUANTITY_UPDATE_FAILED = "Failed to update quantity";
  public static final String FAILED_DB_OPERATION = "Failed DB operation: ";
  public static final String DELETE_OPERATION_CANNOT_BE_PERFORMED =
      "Delete operation cannot be performed on the specified invoice";
  public static final String NO_EXPENSE_WITH_ID = "No Expense With Id";
  public static final String DB_DATA_ISSUE = "Data from database need tech review: ";
  public static final String STOCK_MISMATCH = "Stock mismatch";
  public static final String USER_BALANCE_MISMATCH = "User balance mismatch";
  public static final String DUPLICATE_REPORT_FOR_PAST_DATES =
      "Selected past date have existing report";
  public static final String DUPLICATE_PDF_CANNOT_BE_GENERATED =
      "Duplicate pdf cannot be generated for the requested Id";
  public static final String NO_RECORDS_FOR_THE_REPORT =
      "No records available, so report wont be generated -> ";
  public static final String SALES_REPORT = "DailySalesReport -> ";
  public static final String D_STOCK_REPORT = "DailyStockReport -> ";
  public static final String NO_TRANSACTIONS_FOR_THE_USER = "No transactions for the user -> ";
  public static final String NO_TRANSACTIONS_FOR_THE_DATE =
      "No transactions for the selected date -> ";
  public static final String NO_PRODUCTS_SELECTED_FOR_SALE = "No products selected for sale";
  public static final String PLEASE_SELECT_DATE = "Please select date";
  public static final String PLEASE_SELECT_TYPE = "Please select report type";
  public static final String DUPLICATE_SALE_REQUEST = "Duplicate sale request";

  // General constants
  public static final String APPLICATION_JSON = "application/json";
  public static final String SUCCESS = "Success";
  public static final String DAILY_STOCK_REPORT = "dailyStock";
  public static final String DAILY_BALANCE_REPORT = "dailyBalance";
  public static final String DAILY_SALES_REPORT = "dailySales";
  public static final String MONTHLY_BALANCE_REPORT = "monthlyBalance";
  public static final String MONTHLY_SALES_REPORT = "monthlySales";
  public static final String FOR_PURCHASE = "Stock - ";
  public static final String FOR_SALES = "Sales - ";
  public static final String CONFIG_PREFIX = "svlcf.config";
  public static final String TEXT = "TEXT";
  public static final String FAILED_TO_UPDATE_OR_DELETE_PDF = "Failed to update/delete PDF: ";
  public static final String REPLACE = "replace";
  public static final String USER_DATA = "userData";
  public static final String DATA_BASE_BACKUP = "dataBaseBackup";
  public static final String STOCK_REPORT = "stockReport";
  public static final String FAILED = "Failed";

  // PDF General Constants
  public static final String PDF_EXTENSION = ".pdf";
  public static final String TEMP = "_Temp";
  public static final String DUPLICATE_PATH = "Duplicate/";
  public static final String INVOICE_PATH = "Invoices/";
  public static final String BALANCE_PATH = "Balance/";
  public static final String USER_REPORT_PATH = "UserReport/";
  public static final String STOCK_PATH = "Stock/";
  public static final String SALES_REPORT_PATH = "Sales/";
  public static final String IMAGE_PATH = "Support/InvoiceLogo.png";
  public static final String PREVIOUS_ACCOUNT_BALANCE = "PreviousAccountBalance";
  public static final String PREVIOUS_CASH_BALANCE = "PreviousCashBalance";
  public static final String ACCOUNT_BALANCE = "AccountBalance";
  public static final String CASH_BALANCE = "CashBalance";
  public static final String TOTAL_BALANCE = "TotalBalance: ";
  public static final String TOTAL_DISCOUNT = "DiscountAmount: ";
  public static final String TOTAL_STOCK = "TotalStock";
  public static final String PURCHASE_AMOUNT = "PurchaseAmount: ";
  public static final String PAYMENT_AMOUNT = "PaymentAmount: ";
  public static final String PREVIOUS_YEAR_BALANCE = "PreviousYearBalance";
  public static final String INVOICE_NUMBER_HEADER = "Invoice Number: ";
  public static final String DATE_FORMAT = "yyyy-MM-dd";
  public static final String TIME_FORMAT = "HH:mm:ss";
  public static final String DATE_TEXT = "Date: ";
  public static final String BILL_TYPE = "cash/credit";
  public static final String CUSTOMER_DETAILS_HEADER = "CUSTOMER DETAILS";
  public static final String CUSTOMER_HEADING = "CUSTOMER";
  public static final String TOTAL_BAGS_TEXT = "Total Bags: ";
  public static final String TOTAL_BILL_AMOUNT_TEXT = "Bill Amount: Rs.";
  public static final String PREVIOUS_BALANCE_TEXT = "Prev. Balance: ";
  public static final String AMOUNT_END = "/-";
  public static final String AUTHORIZED_BY_TEXT = "AUTHORIZED BY";
  public static final String AMOUNT_CREDITED_HEADING = "AMOUNT CREDITED";
  public static final String AMOUNT_DEBITED_HEADING = "AMOUNT DEBITED";
  public static final String DAILY_BALANCE_REPORT_HEADING = "Daily Balance Report: ";
  public static final String PRE_BALANCE_REPORT_HEADING = "Previous Year Balance Report: ";
  public static final String DAILY_SALES_REPORT_HEADING = "Daily Sales Report: ";
  public static final String DAILY_STOCK_REPORT_HEADING = "Daily Stock Report: ";
  public static final String USER_REPORT_HEADING = "User Report";
  public static final String ITEM_HEADING = "ITEM";
  public static final String BAGS_HEADING = "BAGS";
  public static final String KGS_HEADING = "KGS";
  public static final String RATE_HEADING = "RATE";
  public static final String AMOUNT_HEADING = "AMOUNT";
  public static final String BALANCE_HEADING = "BALANCE";
  public static final String DETAILS_HEADING = "DETAILS";
  public static final String USER_HEADING = "USER";
  public static final String PAYMENT_MODE_HEADING = "PAYMENT MODE";
  public static final String IN_STOCK_HEADING = "IN STOCK";
  public static final String OUT_STOCK_HEADING = "OUT STOCK";
  public static final String STOCK_SUMMARY_HEADING = "STOCK SUMMARY";
  public static final String SALES_HEADING = "SALES";
  public static final String PAYMENT_HEADING = "PAYMENT";
  public static final String QUANTITY_HEADING = "QUANTITY";
  public static final String PREVIOUS_STOCK_HEADING = "PRE STOCK";
  public static final String CURRENT_STOCK_HEADING = "CURR STOCK";
  public static final String VARIANT_HEADING = "VARIANT";
  public static final String INVOICE_HEADING = "INVOICE";
  public static final String DATE_HEADING = "DATE";
  public static final String TIME_HEADING = "TIME";
  public static final String ID_HEADING = "ID";
  public static final String GSTIN = "GSTIN: ";

  // SVLCF Tables
  public static final String DEL_TRANS_STRING = "delTrans";
  public static final String INVOICE_STRING = "invoice";
  public static final String PAYMENT_STRING = "payment";
  public static final String EXPENSE_STRING = "expense";
  public static final String USER_STRING = "user";
  public static final String STOCK_STRING = "stock";
  public static final String SVLCF_STRING = "svlcf";
  public static final String SALES_STRING = "sales";
  public static final String PRODUCT_STRING = "product";
  public static final String BALANCE_REPORT_STRING = "balanceReport";
  public static final String STOCK_REPORT_STRING = "stockReport";

  // Numerical constants
  public static final Long L_ONE = 1L;
  public static final Long L_STOCK_INVOICE_NUMBER = 9223372036854775807L;
  public static final Long L_TECHNICAL_USERID = 171L;
  public static final int I_ZERO = 0;
  public static final int I_ONE = 1;
  public static final int I_TWO = 2;
  public static final int I_THREE = 3;
  public static final int I_FOUR = 4;
  public static final int I_FIVE = 5;
  public static final int I_SIX = 6;
  public static final int I_TWELVE = 12;
  public static final int I_NINETEEN = 19;
  public static final int I_THIRTY = 30;
  public static final float F_ZERO = 0F;
  public static final float F_ONE_AND_HALF = 1.5F;
  public static final float F_TWO = 2F;
  public static final float F_FOUR = 4F;
  public static final float F_TEN = 10F;
  public static final float F_TWELVE = 12F;
  public static final float F_THIRTEEN = 13F;
  public static final float F_FOURTEEN = 14F;
  public static final float F_FIFTEEN = 15F;
  public static final float F_SIXTEEN = 16F;
  public static final float F_SEVENTEEN = 17F;
  public static final float F_EIGHTEEN = 18F;
  public static final float F_TWENTY = 20F;
  public static final float F_TWENTY_FIVE = 25F;
  public static final float F_FORTY_TWO = 42F;
  public static final float F_FORTY_FIVE = 45F;
  public static final float F_THIRTY = 30F;
  public static final float F_THIRTY_FIVE = 35F;
  public static final float F_FIFTY = 50F;
  public static final float F_FIFTY_FIVE = 55F;
  public static final float F_SEVENTY_FIVE = 75F;
  public static final float F_NINETY = 90F;
  public static final float F_NINETY_EIGHT = 98F;
  public static final float F_HUNDRED = 100F;

  // color codes
  public static final String RED = "#ff2d00";
  public static final String GREEN = "#32cd32";

  // Special Chars Constants
  public static final String SLASH = "/";
  public static final String QUERY_SEPARATOR = "', '";
  public static final String VALUE_IN_LOG = "{}";
  public static final String NEWLINE_STRING = "\n";
  public static final String EMPTY_STRING = " ";
  public static final String COLON_SPACE = ": ";
  public static final String UNDER_SCORE = "_";
  public static final String EQUALSYMBOL = "=";
  public static final String BLANK_STRING = "";
  public static final String OPEN_RECT_BRACES = "[";
  public static final String CLOSED_RECT_BRACES = "]";
  public static final String HYPHEN = "-";
  public static final String HYPHEN_WITH_SPACE = " - ";
  public static final String COMMA = ",";
  public static final String LOG_DATA_REPLACER = "{}";

  // API Constants
  public static final String NEW_USER_ENDPOINT = "/newUser";
  public static final String UPDATE_USER_ENDPOINT = "/updateUser";
  public static final String DELETE_USER_BY_ID_ENDPOINT = "/deleteUser/{id}";
  public static final String GET_USERS_WITH_BALANCE_ENDPOINT =
      "/getUsersWithBalance/{balance}/{userType}";
  public static final String GET_ALL_USERS_ENDPOINT = "/getAllUsers";
  public static final String GET_USER_BY_ID_ENDPOINT = "/getUserById/{id}";
  public static final String NEW_STOCK_ENDPOINT = "/newStock";
  public static final String GET_STOCK_ENDPOINT = "/getStock";
  public static final String OLD_STOCK_ENDPOINT = "/oldStock";
  public static final String UPDATE_STOCK_ENDPOINT =
      "/updateStock/{userId}/{newQuantity}/{newPrice}";
  public static final String GET_VARIANT_FILTER_ENDPOINT =
      "/getVariantFilters/{filterEmptyStock}/{brand}/{name}";
  public static final String GET_ITEM_FILTER_ENDPOINT =
      "/getItemFilters/{filterEmptyStock}/{brand}";
  public static final String GET_BRAND_FILTER_ENDPOINT = "/getBrandFilters/{filterEmptyStock}";
  public static final String GET_MATCHED_STOCK_ENDPOINT = "/getMatchedStock";
  public static final String UPDATE_STOCK_PRICE_ENDPOINT = "/updateStockPrice/{stockId}/{newPrice}";
  public static final String CONFIRM_SALE_ENDPOINT = "/confirmSale";
  public static final String LAST_INVOICE_ENDPOINT = "/lastInvoice/{userId}";
  public static final String DUPLICATE_OR_REPLACE_INVOICE_ENDPOINT =
      "/duplicateOrReplaceInvoice/{type}/{invoiceId}";
  public static final String GET_USER_REPORT_ENDPOINT = "/getUserReport/{id}";
  public static final String GET_ALL_USER_REPORT_ENDPOINT = "/getUserReport";
  public static final String VALIDATE_STOCK_REPORTS = "/validateStockReports";
  public static final String CREATE_DIRECTORIES = "/createDirectories";
  public static final String REPORTS_ENDPOINT = "/reports";
  public static final String NEW_PAYMENT_ENDPOINT = "/newPayment";
  public static final String GET_TODAY_PAYMENTS_ENDPOINT = "/getTodayPayments";
  public static final String DELETE_PAYMENT_BY_ID_ENDPOINT = "/deletePayment/{id}";
  public static final String GET_PREVIOUS_BALANCE_ENDPOINT = "/getPreBalance/{userType}";
  public static final String DELETE_EXPENSE_BY_ID_ENDPOINT = "/deleteExpense/{id}";
  public static final String GET_TODAY_EXPENSE_ENDPOINT = "/getTodayExpense";
  public static final String DEPOSIT_CASH_TO_ACCOUNT_ENDPOINT = "/depositCashToAccount/{amount}";
  public static final String GET_CASH_DEPOSITS_ENDPOINT = "/getCashDeposits";
  public static final String ADD_EXPENSE_ENDPOINT = "/addExpense";
  public static final String DB_BACKUP_ENDPOINT = "/databaseBackUp";
  public static final String DATA_HEALTH_GRAPH_ENDPOINT = "/dataHealthGraph";
  public static final String FILES_ENDPOINT = "/files/{dir}/{filename}";
  public static final String SERVER_FILES_PATH = "http://localhost:9001/svlcf/files/";

  private Constants() {}
}
