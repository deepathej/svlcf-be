package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.CashDeposit;
import com.lv2cd.svlcfbe.entity.DelTrans;
import com.lv2cd.svlcfbe.entity.Expense;
import com.lv2cd.svlcfbe.repository.CashDepositRepository;
import com.lv2cd.svlcfbe.repository.DeletedTransactionRepo;
import com.lv2cd.svlcfbe.repository.ExpenseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock private ExpenseRepository expenseRepository;
  @Mock private SvlcfService svlcfService;
  @Mock private CashDepositRepository cashDepositRepository;
  @Mock private DeletedTransactionRepo deletedTransactionRepo;
  @InjectMocks private ExpenseService expenseService;

  @Test
  void testAddExpenseMethodWhenValidDataIsSentThenDataIsAddedToDB() {
    Expense expenseRequest = getExpenseReq();
    Expense expectedResponse = getExpense();
    expectedResponse.setDate(getCurrentDate());
    when(svlcfService.getAvailableIdByName(EXPENSE_STRING)).thenReturn(L_TEN_THOUSAND);
    when(expenseRepository.save(expectedResponse)).thenReturn(expectedResponse);
    Expense actualExpense = expenseService.addExpense(expenseRequest);
    assertEquals(expectedResponse, actualExpense);
  }

  @Test
  void testGetExpensesForTheDateMethodThenListFromDBIsReturned() {
    String date = getCurrentDate();
    Expense expense = getExpense();
    expense.setDate(date);
    when(expenseRepository.findByDate(date)).thenReturn(List.of(expense));
    List<Expense> expenseList = expenseService.getExpensesForTheDate(date);
    assertEquals(I_ONE, expenseList.size());
    assertEquals(expense, expenseList.get(I_ZERO));
  }

  @Test
  void depositCashToAccountMethodWhenDataNotInDBThenAddItTODB() {
    String date = getCurrentDate();
    CashDeposit cashDeposit = getCashDeposit();
    cashDeposit.setDate(date);
    when(cashDepositRepository.findById(date)).thenReturn(Optional.empty());
    when(cashDepositRepository.save(cashDeposit)).thenReturn(cashDeposit);
    assertEquals(SUCCESS, expenseService.depositCashToAccount(I_TEN_THOUSAND));
  }

  @Test
  void textDeleteExpenseByIdMethodWhenValidIdIsUsedThenDataFromDBIsDeleted() {
    doNothing().when(expenseRepository).deleteById(L_TEN_THOUSAND);
    when(expenseRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(getExpense()));
    when(svlcfService.getAvailableIdByName(DEL_TRANS_STRING)).thenReturn(L_TEN_THOUSAND);
    when(deletedTransactionRepo.save(any()))
        .thenReturn(new DelTrans(L_TEN_THOUSAND, EXPENSE_STRING, getExpense().toString()));
    assertEquals(SUCCESS, expenseService.deleteExpenseById(L_TEN_THOUSAND));
    verify(expenseRepository, times(I_ONE)).deleteById(L_TEN_THOUSAND);
    verify(deletedTransactionRepo, times(I_ONE)).save(any());
  }

  @Test
  void textDeleteExpenseByIdMethodWhenIdNotInDBThenExceptionIsThrown() {
    when(expenseRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    assertEquals(
        FAILED_DB_OPERATION + NO_EXPENSE_WITH_ID, expenseService.deleteExpenseById(L_TEN_THOUSAND));
  }

  @Test
  void textGetCashDeposits() {
    when(cashDepositRepository.findAll()).thenReturn(List.of(getCashDeposit()));
    assertEquals(I_ONE,expenseService.getCashDeposits().size());
  }
}
