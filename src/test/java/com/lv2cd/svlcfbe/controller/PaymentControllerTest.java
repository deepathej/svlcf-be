package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.enums.UserType.SUPPLIER;
import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.entity.Payment;
import com.lv2cd.svlcfbe.service.PaymentService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private PaymentService paymentService;

  @Test
  void testNewPayment() throws Exception {
    Payment payment = getPaymentReq();
    Payment expectedPayment = getPayment();
    when(paymentService.newPayment(payment)).thenReturn(expectedPayment);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(NEW_PAYMENT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(payment)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        expectedPayment,
        asObjectFromString(mvcResult.getResponse().getContentAsString(), Payment.class));
    verify(paymentService, times(I_ONE)).newPayment(payment);
  }

  @Test
  void testGetTodayPayments() throws Exception {
    String date = getCurrentDate();
    when(paymentService.getPaymentForTheDate(date)).thenReturn(List.of(getPayment()));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_TODAY_PAYMENTS_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(paymentService, times(I_ONE)).getPaymentForTheDate(date);
  }

  @Test
  void testDeletePaymentById() throws Exception {
    when(paymentService.deletePaymentById(L_TEN_THOUSAND)).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(delete(DELETE_PAYMENT_BY_ID_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isNoContent())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(paymentService, times(I_ONE)).deletePaymentById(L_TEN_THOUSAND);
  }

  @Test
  void testGetPreviousBalance() throws Exception {
    when(paymentService.getPreviousBalance(SUPPLIER)).thenReturn(TEST_SAMPLE_PATH);
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_PREVIOUS_BALANCE_ENDPOINT, SUPPLIER))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(TEST_SAMPLE_PATH, mvcResult.getResponse().getContentAsString());
    verify(paymentService, times(I_ONE)).getPreviousBalance(SUPPLIER);
  }
}
