package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.Constants.SLASH;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.model.BalanceListWithDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceReportServiceTest {

  @InjectMocks private BalanceReportService balanceReportService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGenerateBalanceReportMethodWhenInvalidFilePathThenExceptionIsThrown() {
    String date = getCurrentDate();
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                balanceReportService.generateBalanceReport(
                    getTheMapFromString(CAAC),
                    List.of(
                        BalanceListWithDate.builder()
                            .inAmountRecords(getExpectedInAmountRecords())
                            .outAmountRecords(getExpectedOutAmountRecords())
                            .date(date)
                            .build())),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + BALANCE_PATH.replace(SLASH, BACK_SLASH)
            + date
            + TEMP
            + ".pdf (The system cannot find the path specified)",
        thrown.getMessage());
  }

  @Test
  void testGenerateBalanceReportMethodWhenInAndOutAmountRecordsAreEmptyThenExceptionIsThrown() {
    String date = getCurrentDate();
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () ->
                balanceReportService.generateBalanceReport(
                    getTheMapFromString(CAAC),
                    List.of(
                        BalanceListWithDate.builder()
                            .inAmountRecords(List.of())
                            .outAmountRecords(List.of())
                            .date(date)
                            .build())),
            EXCEPTION_NOT_THROWN);
    assertEquals(NO_TRANSACTIONS_FOR_THE_DATE + date, thrown.getMessage());
  }

  @Test
  void testGenerateBalanceReportMethodWhenValidFilePathThenBalanceReportIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String balanceReportPath =
        balanceReportService.generateBalanceReport(
            getTheMapFromString(CAAC),
            List.of(
                BalanceListWithDate.builder()
                    .inAmountRecords(getExpectedInAmountRecords())
                    .outAmountRecords(getExpectedOutAmountRecords())
                    .date(getCurrentDate())
                    .build()));
    assertNotNull(balanceReportPath);
    validateAndDeleteFile(balanceReportPath);
  }
}
