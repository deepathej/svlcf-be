package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.repository.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DbBackupService {

  private UserRepository userRepository;
  private PaymentRepository paymentRepository;
  private BalanceReportRepository balanceReportRepository;
  private CashDepositRepository cashDepositRepository;
  private ExpenseRepository expenseRepository;
  private OldStockRepository oldStockRepository;
  private PreBalanceRepository preBalanceRepository;
  private ProductRepository productRepository;
  private SalesRepository salesRepository;
  private StockReportRepository stockReportRepository;
  private StockRepository stockRepository;
  private SvlcfRepository svlcfRepository;
  private DeletedTransactionRepo deletedTransactionRepo;
  private ValidStatusRepo validStatusRepo;
  private final List<Supplier<String>> methods =
      List.of(
          this::userData,
          this::preBalanceData,
          this::stockData,
          this::oldStockData,
          this::salesData,
          this::productData,
          this::paymentData,
          this::expenseData,
          this::cashDepositData,
          this::stockReportData,
          this::balanceReportData,
          this::svlcfData,
          this::delTrans,
          this::validStatus);
  private SvlcfConfig svlcfConfig;

  private static final String INSERT_STMT = "INSERT INTO `svlcfdb`.`";
  private static final String USER_QUERY_START =
      INSERT_STMT
          + "user` (`id`, `address`, `balance`, `gstin`, `name`, `phone_number`, `user_type`) VALUES ";
  private static final String PAYMENT_QUERY_START =
      INSERT_STMT
          + "payment` (`id`, `balance_amount`, `date`, `payment_amount`, `payment_mode`, `payment_type`, `previous_balance`, `time`, `user_id`, `user_type`) VALUES ";
  private static final String STOCK_QUERY_START =
      INSERT_STMT + "stock` (`id`, `brand`, `name`, `price`, `quantity`, `variant`) VALUES ";
  private static final String BALANCE_REPORT_QUERY_START =
      INSERT_STMT + "balance_report` (`id`, `account_balance`, `cash_balance`, `date`) VALUES ";
  private static final String SVLCF_QUERY_START =
      INSERT_STMT + "svlcf` (`name`, `avail_value`) VALUES ";
  private static final String STOCK_REPORT_QUERY_START =
      INSERT_STMT + "stock_report` (`id`, `current_stock`, `date`) VALUES ";
  private static final String SALES_QUERY_START =
      INSERT_STMT
          + "sales` (`invoice_number`, `date`, `product_ids`, `sale_amount`, `user_id`, `user_updated_balance`) VALUES ";
  private static final String PRODUCT_QUERY_START =
      INSERT_STMT
          + "product` (`id`, `amount`, `date`, `invoice_number`, `item`, `price`, `quantity`, `user_id`, `variant`) VALUES ";
  private static final String PRE_BALANCE_QUERY_START =
      INSERT_STMT + "pre_balance` (`user_id`, `balance_amount`) VALUES ";
  private static final String OLD_STOCK_QUERY_START =
      INSERT_STMT + "old_stock` (`name`, `quantity`) VALUES ";
  private static final String EXPENSE_QUERY_START =
      INSERT_STMT + "expense` (`id`, `amount`, `date`, `payment_mode`, `remarks`) VALUES ";
  private static final String CASH_DEPOSIT_QUERY_START =
      INSERT_STMT + "cash_deposit` (`date`, `cash`) VALUES ";
  private static final String DEL_TRANS_QUERY_START =
      INSERT_STMT + "del_trans` (`id`, `data`, `type`) VALUES ";
  private static final String VALID_STATUS_QUERY_START =
      INSERT_STMT + "valid_status` (`name`, `status`) VALUES ";
  private static final String ROW_START = "('";
  private static final String ROW_END = "')";
  private static final String QUERY_END = ";\n";
  private static final String DB_BACKUP_DATA_FILE_PATH = "DbBackUpData/backupdata_";
  private static final String SQL_EXTENSION = ".sql";
  private static final String DB_BACKUP_DATA_ADDED = "Backup data added to created/existed file";

  /**
   * create a backup file and write the backup data to the file
   *
   * @return file path
   */
  public String backUpDBData() {
    log.info("Backing up DB data");
    File file =
        new File(
            svlcfConfig.getPdfOutputRootPath()
                + DB_BACKUP_DATA_FILE_PATH
                + getCurrentDate()
                + SQL_EXTENSION);
    try (FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
      bufferedWriter.write(getFormattedData());
    } catch (IOException exception) {
      throw new CustomInternalServerException(exception.toString());
    }
    log.info(DB_BACKUP_DATA_ADDED);
    return file.getAbsolutePath();
  }

  /**
   * creates backup data
   *
   * @return string will all backup data
   */
  private String getFormattedData() {
    log.info("Formatting all the fetched data from DB");
    return methods.stream().map(Supplier::get).collect(Collectors.joining());
  }

  /**
   * form a query with backup data from valid_status table
   *
   * @return query string with backup data for valid_status table
   */
  private String validStatus() {
    log.info("fetching data from valid_status table");
    return VALID_STATUS_QUERY_START
        + validStatusRepo.findAll().stream()
            .map(validStatus -> formatRows(validStatus.getName(), validStatus.getStatus()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from del_trans table
   *
   * @return query string with backup data for del_trans table
   */
  private String delTrans() {
    log.info("fetching data from del_trans table");
    return DEL_TRANS_QUERY_START
        + deletedTransactionRepo.findAll().stream()
            .map(delTran -> formatRows(delTran.getId(), delTran.getData(), delTran.getType()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from user table
   *
   * @return query string with backup data for user table
   */
  private String userData() {
    log.info("fetching data from user table");
    return USER_QUERY_START
        + userRepository.findAll().stream()
            .map(
                user ->
                    formatRows(
                        user.getId(),
                        user.getAddress(),
                        user.getBalance(),
                        user.getGstin(),
                        user.getName(),
                        user.getPhoneNumber(),
                        user.getUserType()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from payment table
   *
   * @return query string with backup data for payment table
   */
  private String paymentData() {
    log.info("fetching data from payment table");
    return PAYMENT_QUERY_START
        + paymentRepository.findAll().stream()
            .map(
                payment ->
                    formatRows(
                        payment.getId(),
                        payment.getBalanceAmount(),
                        payment.getDate(),
                        payment.getPaymentAmount(),
                        payment.getPaymentMode(),
                        payment.getPaymentType(),
                        payment.getPreviousBalance(),
                        payment.getTime(),
                        payment.getUserId(),
                        payment.getUserType()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from stock table
   *
   * @return query string with backup data for stock table
   */
  private String stockData() {
    log.info("fetching data from stock table");
    return STOCK_QUERY_START
        + stockRepository.findAll().stream()
            .map(
                stock ->
                    formatRows(
                        stock.getId(),
                        stock.getBrand(),
                        stock.getName(),
                        stock.getPrice(),
                        stock.getQuantity(),
                        stock.getVariant()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from balance_report table
   *
   * @return query string with backup data for balance_report table
   */
  private String balanceReportData() {
    log.info("fetching data from balance_report table");
    return BALANCE_REPORT_QUERY_START
        + balanceReportRepository.findAll().stream()
            .map(
                balanceReport ->
                    formatRows(
                        balanceReport.getId(),
                        balanceReport.getAccountBalance(),
                        balanceReport.getCashBalance(),
                        balanceReport.getDate()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from cash_deposit table
   *
   * @return query string with backup data for cash_deposit table
   */
  private String cashDepositData() {
    log.info("fetching data from cash_deposit table");
    return CASH_DEPOSIT_QUERY_START
        + cashDepositRepository.findAll().stream()
            .map(cashDeposit -> formatRows(cashDeposit.getDate(), cashDeposit.getCash()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from expense table
   *
   * @return query string with backup data for expense table
   */
  private String expenseData() {
    log.info("fetching data from expense table");
    return EXPENSE_QUERY_START
        + expenseRepository.findAll().stream()
            .map(
                expense ->
                    formatRows(
                        expense.getId(),
                        expense.getAmount(),
                        expense.getDate(),
                        expense.getPaymentMode(),
                        expense.getRemarks()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from old_stock table
   *
   * @return query string with backup data for old_stock table
   */
  private String oldStockData() {
    log.info("fetching data from old_stock table");
    return OLD_STOCK_QUERY_START
        + oldStockRepository.findAll().stream()
            .map(oldStock -> formatRows(oldStock.getName(), oldStock.getQuantity()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from pre_balance table
   *
   * @return query string with backup data for pre_balance table
   */
  private String preBalanceData() {
    log.info("fetching data from pre_balance table");
    return PRE_BALANCE_QUERY_START
        + preBalanceRepository.findAll().stream()
            .map(preBalance -> formatRows(preBalance.getUserId(), preBalance.getBalanceAmount()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from product table
   *
   * @return query string with backup data for product table
   */
  private String productData() {
    log.info("fetching data from product table");
    return PRODUCT_QUERY_START
        + productRepository.findAll().stream()
            .map(
                product ->
                    formatRows(
                        product.getId(),
                        product.getAmount(),
                        product.getDate(),
                        product.getInvoiceNumber(),
                        product.getItem(),
                        product.getPrice(),
                        product.getQuantity(),
                        product.getUserId(),
                        product.getVariant()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from sales table
   *
   * @return query string with backup data for sales table
   */
  private String salesData() {
    log.info("fetching data from sales table");
    return SALES_QUERY_START
        + salesRepository.findAll().stream()
            .map(
                singleSale ->
                    formatRows(
                        singleSale.getInvoiceNumber(),
                        singleSale.getDate(),
                        singleSale.getProductIds(),
                        singleSale.getSaleAmount(),
                        singleSale.getUserId(),
                        singleSale.getUserUpdatedBalance()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from stock_report table
   *
   * @return query string with backup data for stock_report table
   */
  private String stockReportData() {
    log.info("fetching data from stock_report table");
    return STOCK_REPORT_QUERY_START
        + stockReportRepository.findAll().stream()
            .map(
                stockReport ->
                    formatRows(
                        stockReport.getId(), stockReport.getCurrentStock(), stockReport.getDate()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * form a query with backup data from svlcf table
   *
   * @return query string with backup data for svlcf table
   */
  private String svlcfData() {
    log.info("fetching data from svlcf table");
    return SVLCF_QUERY_START
        + svlcfRepository.findAll().stream()
            .map(svlcf -> formatRows(svlcf.getName(), svlcf.getAvailValue()))
            .collect(Collectors.joining(COMMA + EMPTY_STRING))
        + QUERY_END;
  }

  /**
   * Formatted query data
   *
   * @param values list of columns from different tables
   * @return formatted query
   */
  private String formatRows(Object... values) {
    return ROW_START
        + Arrays.stream(values)
            .map(value -> value == null ? BLANK_STRING : value.toString())
            .collect(Collectors.joining(QUERY_SEPARATOR))
        + ROW_END;
  }
}
