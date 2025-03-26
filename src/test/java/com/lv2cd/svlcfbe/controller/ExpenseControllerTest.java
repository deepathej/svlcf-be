package com.lv2cd.svlcfbe.controller;

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
import com.lv2cd.svlcfbe.entity.CashDeposit;
import com.lv2cd.svlcfbe.entity.Expense;
import com.lv2cd.svlcfbe.service.ExpenseService;
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
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ExpenseService expenseService;

  @Test
  void testAddExpense() throws Exception {
    Expense expenseRequest = getExpenseReq();
    Expense expectedResponse = getExpense();
    expectedResponse.setDate(getCurrentDate());
    when(expenseService.addExpense(expenseRequest)).thenReturn(expectedResponse);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(ADD_EXPENSE_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(expenseRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        expectedResponse,
        asObjectFromString(mvcResult.getResponse().getContentAsString(), Expense.class));
    verify(expenseService, times(I_ONE)).addExpense(expenseRequest);
  }

  @Test
  void testDepositCashToAccount() throws Exception {
    when(expenseService.depositCashToAccount(I_TEN_THOUSAND)).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(get(DEPOSIT_CASH_TO_ACCOUNT_ENDPOINT, I_TEN_THOUSAND))
            .andExpect(status().isCreated())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(expenseService, times(I_ONE)).depositCashToAccount(I_TEN_THOUSAND);
  }

  @Test
  void testGetCashDeposits() throws Exception {
    String date = getCurrentDate();
    CashDeposit cashDeposit = getCashDeposit();
    cashDeposit.setDate(date);
    when(expenseService.getCashDeposits()).thenReturn(List.of(cashDeposit));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_CASH_DEPOSITS_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(expenseService, times(I_ONE)).getCashDeposits();
  }

  @Test
  void testGetTodayExpense() throws Exception {
    String date = getCurrentDate();
    Expense expense = getExpense();
    expense.setDate(date);
    when(expenseService.getExpensesForTheDate(date)).thenReturn(List.of(expense));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_TODAY_EXPENSE_ENDPOINT))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(expenseService, times(I_ONE)).getExpensesForTheDate(date);
  }

  @Test
  void testDeleteExpenseById() throws Exception {
    when(expenseService.deleteExpenseById(L_TEN_THOUSAND)).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(delete(DELETE_EXPENSE_BY_ID_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isNoContent())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(expenseService, times(I_ONE)).deleteExpenseById(L_TEN_THOUSAND);
  }
}
