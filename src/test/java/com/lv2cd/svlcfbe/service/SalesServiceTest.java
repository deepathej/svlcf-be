package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.SALES;
import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.Product;
import com.lv2cd.svlcfbe.entity.Sales;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.SalesListWithDate;
import com.lv2cd.svlcfbe.model.SalesRequest;
import com.lv2cd.svlcfbe.repository.SalesRepository;
import com.lv2cd.svlcfbe.service.generatepdf.InvoiceService;
import com.lv2cd.svlcfbe.service.generatepdf.SalesReportService;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesServiceTest {

  @Mock private SvlcfService svlcfService;
  @Mock private StockService stockService;
  @Mock private ProductService productService;
  @Mock private SalesRepository salesRepository;
  @Mock private InvoiceService invoiceService;
  @Mock private UserService userService;
  @Mock private SalesReportService salesReportService;
  @InjectMocks private SalesService salesService;

  @Test
  void testConfirmSaleMethodWhenSupplierIsUsedForSaleThenExceptionIsThrown() {
    SalesRequest salesRequest = getSupplierSaleReq();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> salesService.confirmSale(salesRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(CANNOT_SALE_TO_SUPPLIER, thrown.getMessage());
  }

  @Test
  void testConfirmSaleMethodWhenEmptyProductListIsSentThenExceptionIsThrown() {
    SalesRequest salesRequest = getConsumerSaleReqWithEmptyProducts();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> salesService.confirmSale(salesRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(NO_PRODUCTS_SELECTED_FOR_SALE, thrown.getMessage());
  }

  @Test
  void testConfirmSaleMethodWhenConsumerIsUsedForSaleThenSaleIsConfirmed() {
    SalesRequest salesRequest = getConsumerSaleReq();
    when(svlcfService.getAvailableIdByName(INVOICE_STRING)).thenReturn(L_TEST_INVOICE_NUMBER);
    Stock stock1 = getStock();
    Stock stock2 = getStock2();
    when(stockService.getStockById(L_TEN_THOUSAND)).thenReturn(stock1);
    when(stockService.getStockById(L_TEN_THOUSAND_AND_ONE)).thenReturn(stock2);
    doNothing().when(stockService).updateQuantityOfStock(L_TEN_THOUSAND, I_TEN);
    doNothing().when(stockService).updateQuantityOfStock(L_TEN_THOUSAND_AND_ONE, I_TEN);
    Product product1 = getProduct();
    product1.setDate(getCurrentDate());
    Product product2 = getProduct2();
    product2.setDate(getCurrentDate());
    when(productService.addProductToRepo(
            salesRequest.getSaleProducts().get(0),
            L_TEN_THOUSAND_AND_ONE,
            L_TEST_INVOICE_NUMBER,
            SALES))
        .thenReturn(product1);
    when(productService.addProductToRepo(
            salesRequest.getSaleProducts().get(1),
            L_TEN_THOUSAND_AND_ONE,
            L_TEST_INVOICE_NUMBER,
            SALES))
        .thenReturn(product2);
    when(userService.updateUserBalance(
            L_TEN_THOUSAND_AND_ONE, I_THREE_THOUSAND_AND_EIGHT_HUNDRED, I_TEN_THOUSAND, SALES))
        .thenReturn(I_TEN_THOUSAND);
    Sales sales = getSales();
    sales.setDate(getCurrentDate());
    when(salesRepository.save(sales)).thenReturn(sales);
    when(invoiceService.generateInvoice(
            salesRequest.getSaleProducts(), L_TEST_INVOICE_NUMBER, salesRequest.getUser()))
        .thenReturn(TEST_SAMPLE_PATH);
    String path = salesService.confirmSale(salesRequest);
    assertEquals(TEST_SAMPLE_PATH, path);
  }

  @Test
  void testConfirmSaleMethodWhenSimilarSaleExistsThenThrowException() {
    SalesRequest salesRequest = getConsumerSaleReq();
    when(svlcfService.getAvailableIdByName(INVOICE_STRING)).thenReturn(L_TEST_INVOICE_NUMBER);
    String date = getCurrentDate();
    Sales sales = getSales();
    sales.setDate(date);
    when(salesRepository.findByDate(date)).thenReturn(List.of(sales));
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> salesService.confirmSale(salesRequest),
            EXCEPTION_NOT_THROWN);
    assertEquals(DUPLICATE_SALE_REQUEST, thrown.getMessage());
  }

  @Test
  void testGetTodaySalesReportMethodWhenDataAvailableInDBForTheDateThenReportIsGenerated() {
    String date = getCurrentDate();
    Sales sales = getSales();
    sales.setDate(date);
    when(salesRepository.findByDate(date)).thenReturn(List.of(sales));
    Map<Long, String> map = new HashMap<>();
    map.put(L_TEN_THOUSAND_AND_ONE, getConsumerWithBalance().getName());
    when(userService.getUserIdAndNameMap()).thenReturn(map);
    when(productService.getProductById(L_TEN_THOUSAND)).thenReturn(getProduct());
    when(productService.getProductById(L_TEN_THOUSAND_AND_ONE)).thenReturn(getProduct2());
    when(salesReportService.generateSalesReport(
            List.of(
                SalesListWithDate.builder()
                    .saleDetailForDayList(List.of(getSaleDetailForDay()))
                    .date(getCurrentDate())
                    .build())))
        .thenReturn(TEST_SAMPLE_PATH);
    assertEquals(TEST_SAMPLE_PATH, salesService.getSalesReportForTheDate(date));
  }

  @Test
  void testGenerateDuplicateInvoiceMethodWhenRequestedIdDontHaveInvoiceThenThrowException() {
    when(svlcfService.getOnlyAvailableIdByName(INVOICE_STRING)).thenReturn(L_ONE);
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () -> salesService.generateDuplicateInvoice(L_TWO),
            EXCEPTION_NOT_THROWN);
    assertEquals(DUPLICATE_PDF_CANNOT_BE_GENERATED, thrown.getMessage());
  }

  @Test
  void testGenerateDuplicateInvoiceMethodWhenGetSalesFromDbFailsThenThrowException() {
    when(svlcfService.getOnlyAvailableIdByName(INVOICE_STRING)).thenReturn(L_TWO);
    when(salesRepository.findById(L_ONE)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> salesService.generateDuplicateInvoice(L_ONE),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + SALES_STRING, thrown.getMessage());
  }

  @Test
  void testGenerateDuplicateInvoiceMethodWhenSalesDataAvailableThenReturnPDFPath() {
    when(svlcfService.getOnlyAvailableIdByName(INVOICE_STRING))
        .thenReturn(L_TEST_INVOICE_NUMBER + L_ONE);
    when(salesRepository.findById(L_TEST_INVOICE_NUMBER)).thenReturn(Optional.of(getSales()));
    when(productService.getProductById(L_TEN_THOUSAND)).thenReturn(getProduct());
    when(productService.getProductById(L_TEN_THOUSAND_AND_ONE)).thenReturn(getProduct2());
    when(stockService.getMatchedStock(BRAND, ITEM_NAME, I_FIFTY)).thenReturn(getStock());
    when(stockService.getMatchedStock(BRAND, ITEM_NAME2, I_FORTY)).thenReturn(getStock2());
    when(userService.getUserById(L_TEN_THOUSAND_AND_ONE)).thenReturn(getConsumerWithBalance());
    Sales sales = getSales();
    User user = getConsumerWithBalance();
    user.setBalance(sales.getUserUpdatedBalance() - sales.getSaleAmount());
    Stock stock1 = getStock();
    stock1.setQuantity(stock1.getQuantity() - I_TEN);
    Stock stock2 = getStock2();
    stock2.setQuantity(stock2.getQuantity() - I_TEN);
    when(invoiceService.generateInvoice(
            List.of(stock1, stock2), L_TEST_INVOICE_NUMBER, user, sales.getDate()))
        .thenReturn(TEST_SAMPLE_PATH);
    assertNotNull(TEST_SAMPLE_PATH, salesService.generateDuplicateInvoice(L_TEST_INVOICE_NUMBER));
  }

  @Test
  void testUpdateExistingInvoiceWithTech(){
    when(salesRepository.findAll()).thenReturn(List.of(getSales()));
    Exception thrown = assertThrows(CustomBadRequestException.class,
            () -> salesService.updateExistingInvoiceWithTech(L_TEST_INVOICE_NUMBER + L_ONE), EXCEPTION_NOT_THROWN);
    assertEquals(DELETE_OPERATION_CANNOT_BE_PERFORMED, thrown.getMessage());
  }

  @Test
  void testUpdateExistingInvoiceButNotLatest(){
    Sales sales = getSales();
    sales.setInvoiceNumber(sales.getInvoiceNumber() + L_ONE);
    when(salesRepository.findAll()).thenReturn(List.of(getSales(), sales));
    Exception thrown = assertThrows(CustomBadRequestException.class,
            () -> salesService.updateExistingInvoiceWithTech(L_TEST_INVOICE_NUMBER), EXCEPTION_NOT_THROWN);
    assertEquals(DELETE_OPERATION_CANNOT_BE_PERFORMED, thrown.getMessage());
  }

  /*@Test
  void testUpdateExistingInvoiceWithTechSuccess(){
    when(salesRepository.findAll()).thenReturn(List.of(getSales()));
    doNothing().when(salesRepository).updateTechDetailsForSale(L_TEST_INVOICE_NUMBER, L_TECHNICAL_USERID, I_ZERO,
            I_ZERO);
    String str = salesService.updateExistingInvoiceWithTech(L_TEST_INVOICE_NUMBER);
  }*/

  @Test
  void testGetLastSaleInvoiceWhenNoInvoice(){
    when(salesRepository.findAll()).thenReturn(List.of());
    assertEquals("http://localhost:9001/svlcf/files/Invoices/0.pdf", salesService.getLastSaleInvoice(L_TEN_THOUSAND_AND_ONE));
  }

  @Test
  void testGetLastSaleInvoiceWhenInvoiceAvailable(){
    when(salesRepository.findAll()).thenReturn(List.of(getSales()));
    assertEquals("http://localhost:9001/svlcf/files/Invoices/9223372036854775806.pdf", salesService.getLastSaleInvoice(L_TEN_THOUSAND_AND_ONE));
  }
}
