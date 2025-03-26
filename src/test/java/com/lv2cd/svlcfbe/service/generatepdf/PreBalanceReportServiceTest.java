package com.lv2cd.svlcfbe.service.generatepdf;

import static com.lv2cd.svlcfbe.enums.UserType.CONSUMER;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.getPreBalanceMap;
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
class PreBalanceReportServiceTest {

  @InjectMocks private PreBalanceReportService preBalanceReportService;
  @Mock private SvlcfConfig svlcfConfig;

  @Test
  void testGeneratePreBalanceReportMethodWhenInvalidFilePathThenExceptionIsThrown() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(INVALID_ROOT_PATH);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> preBalanceReportService.generatePreBalanceReport(getPreBalanceMap(), CONSUMER),
            EXCEPTION_NOT_THROWN);
    assertEquals(
        INVALID_ROOT_PATH
            + BALANCE_PATH.replace(SLASH, BACK_SLASH)
            + CONSUMER
            + TEMP
            + ERROR_EXTENSION_FOR_PDF_GENERATORS,
        thrown.getMessage());
  }

  @Test
  void testGeneratePreBalanceReportMethodWhenValidFilePathThenPreBalanceReportIsGenerated() {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(UNIT_TEST_ROOT_PATH);
    String preBalancePath =
        preBalanceReportService.generatePreBalanceReport(getPreBalanceMap(), CONSUMER);
    assertNotNull(preBalancePath);
    validateAndDeleteFile(preBalancePath);
  }
}
