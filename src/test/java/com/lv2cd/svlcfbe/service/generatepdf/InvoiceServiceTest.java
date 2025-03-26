package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.SalesRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

  @InjectMocks private InvoiceService invoiceService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGenerateInvoiceMethodWhenInvalidFilePathThenExceptionIsThrown() {
    SalesRequest salesRequest = getConsumerSaleReq();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                invoiceService.generateInvoice(
                    salesRequest.getSaleProducts(), L_TEST_INVOICE_NUMBER, salesRequest.getUser()),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + INVOICE_PATH.replace(SLASH, BACK_SLASH)
            + L_TEST_INVOICE_NUMBER
            + ERROR_EXTENSION_FOR_PDF_GENERATORS,
        thrown.getMessage());
  }

  @Test
  void testGenerateInvoiceMethodWhenValidFilePathThenInvoiceIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    SalesRequest salesRequest = getConsumerSaleReq();
    String invoicePath =
        invoiceService.generateInvoice(
            salesRequest.getSaleProducts(), L_TEST_INVOICE_NUMBER, salesRequest.getUser());
    assertNotNull(invoicePath);
    validateAndDeleteFile(invoicePath);
  }

  @Test
  void testGenerateInvoiceMethodWhenUserWithGstinEmptyThenInvoiceIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    SalesRequest salesRequest = getConsumerSaleReq();
    User user = salesRequest.getUser();
    user.setGstin(BLANK_STRING);
    String invoicePath =
        invoiceService.generateInvoice(salesRequest.getSaleProducts(), L_TEST_INVOICE_NUMBER, user);
    assertNotNull(invoicePath);
    validateAndDeleteFile(invoicePath);
  }

  @Test
  void testGenerateInvoiceMethodWhenUserWithGstinNullThenInvoiceIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    SalesRequest salesRequest = getConsumerSaleReq();
    User user = salesRequest.getUser();
    user.setGstin(null);
    String invoicePath =
        invoiceService.generateInvoice(salesRequest.getSaleProducts(), L_TEST_INVOICE_NUMBER, user);
    assertNotNull(invoicePath);
    validateAndDeleteFile(invoicePath);
  }

  @Test
  void testGenerateInvoiceMethodWhenProductsBetweenFiveAndThirteenThenInvoiceIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    SalesRequest salesRequest = getConsumerSaleReq();
    Stock stock = getStock();
    Stock stock2 = getStock2();
    Stock stock3 = getStock3();
    Stock stock4 = getStock4();
    List<Stock> productList = List.of(stock, stock2, stock3, stock4, stock, stock2, stock3, stock4);
    String invoicePath =
        invoiceService.generateInvoice(productList, L_TEST_INVOICE_NUMBER, salesRequest.getUser());
    assertNotNull(invoicePath);
    validateAndDeleteFile(invoicePath);
  }

  @Test
  void testGenerateInvoiceMethodWhenProductsMoreThanThirteenThenInvoiceIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    SalesRequest salesRequest = getConsumerSaleReq();
    Stock stock = getStock();
    Stock stock2 = getStock2();
    Stock stock3 = getStock3();
    Stock stock4 = getStock4();
    List<Stock> productList =
        List.of(
            stock, stock2, stock3, stock4, stock, stock2, stock3, stock4, stock, stock2, stock3,
            stock4, stock, stock2, stock3, stock4);
    String invoicePath =
        invoiceService.generateInvoice(productList, L_TEST_INVOICE_NUMBER, salesRequest.getUser());
    assertNotNull(invoicePath);
    validateAndDeleteFile(invoicePath);
  }
}
