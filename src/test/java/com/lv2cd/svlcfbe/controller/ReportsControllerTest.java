package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.model.ReportsRequest;
import com.lv2cd.svlcfbe.service.ReportsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(ReportsController.class)
class ReportsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ReportsService reportsService;

  @Test
  void testDailyReports() throws Exception {
    ReportsRequest reportsRequest = getStockReportReq();
    when(reportsService.reports(reportsRequest)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(REPORTS_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(reportsRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(reportsService, times(I_ONE)).reports(reportsRequest);
  }

  @Test
  void testGetUserReport() throws Exception {
    when(reportsService.getUserReport(L_TEN_THOUSAND, false)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_USER_REPORT_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(reportsService, times(I_ONE)).getUserReport(L_TEN_THOUSAND, false);
  }

  @Test
  void testGetAllUserReports() throws Exception {
    when(reportsService.getAllUserReports()).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_ALL_USER_REPORT_ENDPOINT))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(reportsService, times(I_ONE)).getAllUserReports();
  }

  @Test
  void testValidateStockReports() throws Exception {
    when(reportsService.validateStockReports()).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(get(VALIDATE_STOCK_REPORTS))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(reportsService, times(I_ONE)).validateStockReports();
  }

  @Test
  void testCreateDirectories() throws Exception {
    when(reportsService.createDirectories()).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(get(CREATE_DIRECTORIES))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(reportsService, times(I_ONE)).createDirectories();
  }
}
