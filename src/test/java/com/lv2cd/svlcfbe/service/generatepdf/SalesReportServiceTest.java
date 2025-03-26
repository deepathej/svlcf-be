package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.SaleDetailForDay;
import com.lv2cd.svlcfbe.model.SalesListWithDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesReportServiceTest {

  @InjectMocks private SalesReportService salesReportService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGenerateSalesReportMethodWhenInvalidFilePathThenExceptionIsThrown() {
    String date = getCurrentDate();
    SaleDetailForDay currentDaySale = getSaleDetailForDay();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                salesReportService.generateSalesReport(
                    List.of(
                        SalesListWithDate.builder()
                            .saleDetailForDayList(List.of(currentDaySale))
                            .date(date)
                            .build())),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + SALES_REPORT_PATH.replace(SLASH, BACK_SLASH)
            + date
            + TEMP
            + ERROR_EXTENSION_FOR_PDF_GENERATORS,
        thrown.getMessage());
  }

  @Test
  void testGenerateSalesReportMethodWhenCurrentDaySalesIsEmptyThenExceptionIsThrown() {
    String date = getCurrentDate();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () ->
                salesReportService.generateSalesReport(
                    List.of(
                        SalesListWithDate.builder()
                            .saleDetailForDayList(List.of())
                            .date(date)
                            .build())),
            EXCEPTION_NOT_THROWN);
    assertEquals(NO_RECORDS_FOR_THE_REPORT + SALES_REPORT + date, thrown.getMessage());
  }

  @Test
  void testGenerateSalesReportMethodWhenValidFilePathThenSalesReportIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String salesReportPath =
        salesReportService.generateSalesReport(
            List.of(
                SalesListWithDate.builder()
                    .saleDetailForDayList(List.of(getSaleDetailForDay()))
                    .date(getCurrentDate())
                    .build()));
    assertNotNull(salesReportPath);
    validateAndDeleteFile(salesReportPath);
  }

  @Test
  void testGenerateSalesReportMethodWhenForBlankPageThenBlankPageIsRemoved() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String salesReportPath =
        salesReportService.generateSalesReport(
            List.of(
                SalesListWithDate.builder()
                    .saleDetailForDayList(getCurrentDaySaleList())
                    .date(getCurrentDate())
                    .build()));
    assertNotNull(salesReportPath);
    validateAndDeleteFile(salesReportPath);
  }

  @Test
  void
      testGenerateSalesReportMethodWhenAlreadyReportExistsForTheDayThenExistingReportIsDeletedBlankPageIsRemoved() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    salesReportService.generateSalesReport(
        List.of(
            SalesListWithDate.builder()
                .saleDetailForDayList(getCurrentDaySaleList())
                .date(getCurrentDate())
                .build()));
    String salesReportPath2 =
        salesReportService.generateSalesReport(
            List.of(
                SalesListWithDate.builder()
                    .saleDetailForDayList(getCurrentDaySaleList())
                    .date(getCurrentDate())
                    .build()));
    assertNotNull(salesReportPath2);
    validateAndDeleteFile(salesReportPath2);
  }

  private List<SaleDetailForDay> getCurrentDaySaleList() {
    SaleDetailForDay saleDetailForDay = getSaleDetailForDay();
    return List.of(
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay,
        saleDetailForDay);
  }
}
