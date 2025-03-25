package com.lv2cd.svlcfbe.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.lv2cd.svlcfbe.util.TestConstants.TEST_SAMPLE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class SvlcfConfigTest {

  @InjectMocks private SvlcfConfig svlcfConfig;

  @Test
  void testSetPdfOutputRootPathAndVerify() {
    svlcfConfig.setPdfOutputRootPath(TEST_SAMPLE_PATH);
    assertEquals(TEST_SAMPLE_PATH, svlcfConfig.getPdfOutputRootPath());
  }
}
