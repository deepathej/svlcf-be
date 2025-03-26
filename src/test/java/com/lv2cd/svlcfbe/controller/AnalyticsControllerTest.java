package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.asObjectFromString;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.getDataHealthGraph;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.model.DataHealthGraph;
import com.lv2cd.svlcfbe.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private AnalyticsService analyticsService;

  @Test
  void testGetDataHealthGraph() throws Exception {
    DataHealthGraph dataHealthGraph = getDataHealthGraph();
    when(analyticsService.getDataHealthGraph()).thenReturn(dataHealthGraph);
    MvcResult mvcResult =
        mockMvc
            .perform(get(DATA_HEALTH_GRAPH_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(dataHealthGraph, asObjectFromString(mvcResult.getResponse().getContentAsString(),
            DataHealthGraph.class));
    verify(analyticsService, times(I_ONE)).getDataHealthGraph();
  }
}
