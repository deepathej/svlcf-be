package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.Constants.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.config.SvlcfConfig;
import com.lv2cd.svlcfbe.util.TestConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(FilesToServerController.class)
class FilesToServerControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private SvlcfConfig svlcfConfig;

  @Test
  void testGetFileError() throws Exception {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn(TestConstants.TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(FILES_ENDPOINT, "SampleDir", "SampleFile"))
            .andExpect(status().isNotFound())
            .andDo(print())
            .andReturn();
    verify(svlcfConfig, times(I_ONE)).getPdfOutputRootPath();
  }

  @Test
  void testGetFileSuccess() throws Exception {
    when(svlcfConfig.getPdfOutputRootPath()).thenReturn("C:/Data/OneDrive/");
    MvcResult mvcResult =
        mockMvc
            .perform(get(FILES_ENDPOINT, "UnitTest", "SampleFile.pdf"))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    verify(svlcfConfig, times(I_ONE)).getPdfOutputRootPath();
  }
}
