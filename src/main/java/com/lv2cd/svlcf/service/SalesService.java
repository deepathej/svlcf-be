package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.enums.PaymentType.SALES;
import static com.lv2cd.svlcf.enums.UserType.*;
import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.*;
import com.lv2cd.svlcf.exception.CustomBadRequestException;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import com.lv2cd.svlcf.model.SaleDetailForDay;
import com.lv2cd.svlcf.model.SalesListWithDate;
import com.lv2cd.svlcf.model.SalesRequest;
import com.lv2cd.svlcf.repository.DeletedTransactionRepo;
import com.lv2cd.svlcf.repository.SalesRepository;
import com.lv2cd.svlcf.service.generatepdf.InvoiceService;
import com.lv2cd.svlcf.service.generatepdf.SalesReportService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class SalesService {

  private SalesRepository salesRepository;
  private UserService userService;
  private SvlcfService svlcfService;
  private ProductService productService;
  private StockService stockService;
  private InvoiceService invoiceService;
  private SalesReportService salesReportService;
  private DeletedTransactionRepo deletedTransactionRepo;

  private static void validateSaleRequest(SalesRequest salesRequest, User user) {
    log.info("Validating sale request");
    if (user.getUserType() == SUPPLIER) {
      throw new CustomBadRequestException(CANNOT_SALE_TO_SUPPLIER);
    }
    if (salesRequest.getSaleProducts().isEmpty()) {
      throw new CustomBadRequestException(NO_PRODUCTS_SELECTED_FOR_SALE);
    }
  }

  private static int calculateSaleAmount(SalesRequest salesRequest) {
    log.info("Calculating sale amount");
    return salesRequest.getSaleProducts().stream()
        .map(saleProduct -> saleProduct.getPrice() * saleProduct.getQuantity())
        .mapToInt(Integer::intValue)
        .sum();
  }

  private static Sales createSaleRecord(
      Long invoiceNumber,
      List<Long> productIds,
      User user,
      Integer saleAmount,
      Integer newBalance,
      String date) {
    log.info("Creating sale record");
    return new Sales(
        invoiceNumber,
        productIds
            .toString()
            .replace(OPEN_RECT_BRACES, BLANK_STRING)
            .replace(CLOSED_RECT_BRACES, BLANK_STRING),
        user.getId(),
        saleAmount,
        newBalance,
        date);
  }

  public String confirmSale(SalesRequest salesRequest) {
    log.info("confirm sale {}", writeAsJson(salesRequest));
    User user = salesRequest.getUser();
    String date = getCurrentDate();
    validateSaleRequest(salesRequest, user);
    Long invoiceNumber = svlcfService.getAvailableIdByName(INVOICE_STRING);
    Integer saleAmount = calculateSaleAmount(salesRequest);

    checkForDuplicateSale(user.getId(), date, saleAmount);

    List<Long> productIds =
        saveIndividualProductsInDB(salesRequest.getSaleProducts(), user.getId(), invoiceNumber);
    Integer newBalance =
        userService.updateUserBalance(user.getId(), saleAmount, user.getBalance(), SALES);
    Sales sales = createSaleRecord(invoiceNumber, productIds, user, saleAmount, newBalance, date);
    salesRepository.save(sales);
    return invoiceService.generateInvoice(salesRequest.getSaleProducts(), invoiceNumber, user);
  }

  private void checkForDuplicateSale(Long id, String date, Integer saleAmount) {
    log.info("Verifying to check if this is a duplicate sale");
    if (getSaleReportForTheDate(date).stream()
        .anyMatch(
            sales -> sales.getUserId().equals(id) && sales.getSaleAmount().equals(saleAmount))) {
      throw new CustomBadRequestException(DUPLICATE_SALE_REQUEST);
    }
  }

  private List<Long> saveIndividualProductsInDB(
      List<Stock> products, Long userId, Long invoiceNumber) {
    return products.stream()
        .map(
            saleProduct -> {
              Stock currentStock = stockService.getStockById(saleProduct.getId());
              stockService.updateQuantityOfStock(
                  saleProduct.getId(), currentStock.getQuantity() - saleProduct.getQuantity());
              return productService
                  .addProductToRepo(saleProduct, userId, invoiceNumber, SALES)
                  .getId();
            })
        .toList();
  }

  public String getSalesReportForTheMonth(Stream<String> dates) {
    return salesReportService.generateSalesReport(
        dates.parallel().map(this::getSalesDataForTheDate).toList());
  }

  public String getSalesReportForTheDate(String date) {
    return salesReportService.generateSalesReport(List.of(getSalesDataForTheDate(date)));
  }

  private List<Sales> getSaleReportForTheDate(String date) {
    return salesRepository.findByDate(date);
  }

  private SalesListWithDate getSalesDataForTheDate(String date) {
    Map<Long, String> userIdAndNameMap = userService.getUserIdAndNameMap();
    List<SaleDetailForDay> saleDetailForDays =
        getSaleReportForTheDate(date).stream()
            .map(
                sales -> {
                  String products =
                      Arrays.stream(
                              sales
                                  .getProductIds()
                                  .replace(COMMA, BLANK_STRING)
                                  .split(EMPTY_STRING))
                          .sorted()
                          .parallel()
                          .map(
                              productId -> {
                                Product product =
                                    productService.getProductById(Long.valueOf(productId));
                                return product.getItem().replace(UNDER_SCORE, HYPHEN)
                                    + HYPHEN
                                    + product.getVariant()
                                    + HYPHEN
                                    + product.getQuantity()
                                    + HYPHEN
                                    + product.getAmount();
                              })
                          .toList()
                          .toString()
                          .replace(OPEN_RECT_BRACES, BLANK_STRING)
                          .replace(CLOSED_RECT_BRACES, EMPTY_STRING)
                          .replace(COMMA + EMPTY_STRING, NEWLINE_STRING);
                  return SaleDetailForDay.builder()
                      .invoiceNumber(sales.getInvoiceNumber())
                      .userId(sales.getUserId())
                      .name(userIdAndNameMap.get(sales.getUserId()))
                      .saleAmount(sales.getSaleAmount())
                      .itemDetails(products)
                      .build();
                })
            .toList();
    return SalesListWithDate.builder().saleDetailForDayList(saleDetailForDays).date(date).build();
  }

  public String generateDuplicateInvoice(Long invoiceId) {
    if (svlcfService.getOnlyAvailableIdByName(INVOICE_STRING) <= invoiceId) {
      throw new CustomBadRequestException(DUPLICATE_PDF_CANNOT_BE_GENERATED);
    }
    Sales sales =
        salesRepository
            .findById(invoiceId)
            .orElseThrow(
                () -> new CustomInternalServerException(FAILED_DB_OPERATION + SALES_STRING));
    List<Stock> stockList =
        Arrays.stream(sales.getProductIds().replace(EMPTY_STRING, BLANK_STRING).split(COMMA))
            .map(
                id -> {
                  Product product = productService.getProductById(Long.valueOf(id));
                  String[] brandAndName = product.getItem().split(UNDER_SCORE);
                  Stock stock =
                      stockService.getMatchedStock(
                          brandAndName[I_ZERO], brandAndName[I_ONE], product.getVariant());
                  stock.setQuantity(product.getQuantity());
                  stock.setPrice(product.getPrice());
                  return stock;
                })
            .toList();
    User user = userService.getUserById(sales.getUserId());
    user.setBalance(sales.getUserUpdatedBalance() - sales.getSaleAmount());
    return invoiceService.generateInvoice(stockList, invoiceId, user, sales.getDate());
  }

  public String updateExistingInvoiceWithTech(Long invoiceId) {
    List<Sales> salesList = salesRepository.findAll();
    Sales sales =
        salesList.stream()
            .filter(sale -> sale.getInvoiceNumber().equals(invoiceId))
            .findFirst()
            .orElseThrow(() -> new CustomBadRequestException(DELETE_OPERATION_CANNOT_BE_PERFORMED));
    if (salesList.stream()
        .filter(sale -> Objects.equals(sale.getUserId(), sales.getUserId()))
        .map(Sales::getInvoiceNumber)
        .noneMatch(invoiceNumber -> invoiceNumber > invoiceId)) {
      salesRepository.updateTechDetailsForSale(invoiceId, L_TECHNICAL_USERID, I_ZERO, I_ZERO);
      StringBuilder stringBuilder = updateProductDetailsWithTechUser(sales);
      userService.directUpdateUserBalance(
          sales.getUserId(),
          userService.getUserById(sales.getUserId()).getBalance() - sales.getSaleAmount());
      deletedTransactionRepo.save(
          new DelTrans(
              svlcfService.getAvailableIdByName(DEL_TRANS_STRING),
              SALES_STRING,
              writeAsJson(sales) + stringBuilder));
      return generateDuplicateInvoice(invoiceId);
    }
    throw new CustomBadRequestException(DELETE_OPERATION_CANNOT_BE_PERFORMED);
  }

  private StringBuilder updateProductDetailsWithTechUser(Sales sales) {
    StringBuilder stringBuilder = new StringBuilder();
    Arrays.stream(sales.getProductIds().replace(EMPTY_STRING, BLANK_STRING).split(COMMA))
        .mapToLong(Long::parseLong)
        .forEach(
            productId -> {
              Product product = productService.getProductById(productId);
              stringBuilder.append(writeAsJson(product));
              List<String> stockItem = Arrays.stream(product.getItem().split(UNDER_SCORE)).toList();
              Stock stock =
                  stockService.getMatchedStock(
                      stockItem.get(I_ONE), stockItem.get(I_ZERO), product.getVariant());
              stockService.updateQuantityOfStock(
                  stock.getId(), stock.getQuantity() + product.getQuantity());
              productService.updateTechDetailsForProduct(
                  productId, L_TECHNICAL_USERID, I_ZERO, I_ZERO);
            });
    return stringBuilder;
  }

  public String getLastSaleInvoice(Long userId) {
    long pastInvoice =
        salesRepository.findAll().stream()
            .filter(sales -> Objects.equals(sales.getUserId(), userId))
            .mapToLong(Sales::getInvoiceNumber)
            .max()
            .orElse(0);
    return SERVER_FILES_PATH + INVOICE_PATH + pastInvoice + PDF_EXTENSION;
  }
}
