package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.entity.CashDeposit;
import com.lv2cd.svlcfbe.entity.Expense;
import com.lv2cd.svlcfbe.service.ExpenseService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class ExpenseController {

  private ExpenseService expenseService;

  /**
   * API is used to add new expense to the DB
   *
   * @param expense object without the id
   * @return expense object with id
   */
  @PostMapping(
      value = ADD_EXPENSE_ENDPOINT,
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON)
  public ResponseEntity<Expense> addExpense(@RequestBody Expense expense) {
    return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(expense));
  }

  /**
   * API is used get all the expenses for the day
   *
   * @return list of all the expenses for the day
   */
  @GetMapping(value = GET_TODAY_EXPENSE_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<Expense>> getTodayExpense() {
    return ResponseEntity.ok(expenseService.getExpensesForTheDate(getCurrentDate()));
  }

  /**
   * API is used to delete the expense from the DB
   *
   * @param id unique id for the expense
   * @return success message
   */
  @DeleteMapping(DELETE_EXPENSE_BY_ID_ENDPOINT)
  public ResponseEntity<String> deleteExpenseById(@PathVariable Long id) {
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(expenseService.deleteExpenseById(id));
  }

  /**
   * API is used to record the cash deposit to account so that the balance sheet shows accurate
   * information
   *
   * @param amount greater than zero
   * @return success message
   */
  @GetMapping(value = DEPOSIT_CASH_TO_ACCOUNT_ENDPOINT)
  public ResponseEntity<String> depositCashToAccount(@PathVariable Integer amount) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(expenseService.depositCashToAccount(amount));
  }

  /**
   * API is used to get the list of cash deposits
   *
   * @return List of CashDeposit records
   */
  @GetMapping(value = GET_CASH_DEPOSITS_ENDPOINT)
  public ResponseEntity<List<CashDeposit>> getCashDeposits() {
    return ResponseEntity.ok(expenseService.getCashDeposits());
  }
}
