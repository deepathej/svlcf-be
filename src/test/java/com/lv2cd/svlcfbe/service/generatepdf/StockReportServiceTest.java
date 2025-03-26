package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.getTheMapFromString;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.validateAndDeleteFile;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockReportServiceTest {

  @InjectMocks private StockReportService stockReportService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGenerateStockReportMethodWhenInvalidFilePathThenExceptionIsThrown() {
    String date = getCurrentDate();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                stockReportService.generateStockReport(
                    date,
                    getTheMapFromString(PREVIOUS_STOCK),
                    getTheMapFromString(IN_STOCK_RECORDS),
                    getTheMapFromString(OUT_STOCK_RECORDS),
                    getTheMapFromString(GENERATED_CURRENT_STOCK)),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + STOCK_PATH.replace(SLASH, BACK_SLASH)
            + date
            + TEMP
            + ERROR_EXTENSION_FOR_PDF_GENERATORS,
        thrown.getMessage());
  }

  @Test
  void testGenerateStockReportMethodWhenValidFilePathThenStockReportIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String stockReportPath =
        stockReportService.generateStockReport(
            getCurrentDate(),
            getTheMapFromString(PREVIOUS_STOCK),
            getTheMapFromString(IN_STOCK_RECORDS),
            getTheMapFromString(OUT_STOCK_RECORDS),
            getTheMapFromString(GENERATED_CURRENT_STOCK));
    assertNotNull(stockReportPath);
    validateAndDeleteFile(stockReportPath);
  }
}
