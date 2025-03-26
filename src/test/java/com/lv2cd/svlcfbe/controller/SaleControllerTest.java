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
import com.lv2cd.svlcfbe.model.SalesRequest;
import com.lv2cd.svlcfbe.service.SalesService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(SaleController.class)
class SaleControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private SalesService salesService;

  @Test
  void testConfirmSale() throws Exception {
    SalesRequest salesRequest = getConsumerSaleReq();
    when(salesService.confirmSale(salesRequest)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(CONFIRM_SALE_ENDPOINT)
                    .contentType(APPLICATION_JSON)
                    .content(asJsonString(salesRequest)))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(salesService, times(I_ONE)).confirmSale(salesRequest);
  }

  @Test
  void testGenerateDuplicateInvoice() throws Exception {
    when(salesService.generateDuplicateInvoice(L_TEN_THOUSAND)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(DUPLICATE_OR_REPLACE_INVOICE_ENDPOINT, SALES_STRING, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(salesService, times(I_ONE)).generateDuplicateInvoice(L_TEN_THOUSAND);
  }

  @Test
  void testReplaceInvoice() throws Exception {
    when(salesService.updateExistingInvoiceWithTech(L_TEN_THOUSAND)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(DUPLICATE_OR_REPLACE_INVOICE_ENDPOINT, REPLACE, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(salesService, times(I_ONE)).updateExistingInvoiceWithTech(L_TEN_THOUSAND);
  }

  @Test
  void testGetLastSaleInvoice() throws Exception {
    when(salesService.getLastSaleInvoice(L_TEN_THOUSAND))
        .thenReturn(SERVER_FILES_PATH + INVOICE_PATH + I_TEN_THOUSAND + PDF_EXTENSION);
    MvcResult mvcResult =
        mockMvc
            .perform(get(LAST_INVOICE_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(
        SERVER_FILES_PATH + INVOICE_PATH + I_TEN_THOUSAND + PDF_EXTENSION,
        mvcResult.getResponse().getContentAsString());
    verify(salesService, times(I_ONE)).getLastSaleInvoice(L_TEN_THOUSAND);
  }
}
