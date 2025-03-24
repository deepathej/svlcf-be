package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.util.CommonMethods.capString;
import static com.lv2cd.svlcf.util.CommonMethods.getCurrentDate;
import static com.lv2cd.svlcf.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.CashDeposit;
import com.lv2cd.svlcf.entity.DelTrans;
import com.lv2cd.svlcf.entity.Expense;
import com.lv2cd.svlcf.repository.CashDepositRepository;
import com.lv2cd.svlcf.repository.DeletedTransactionRepo;
import com.lv2cd.svlcf.repository.ExpenseRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class ExpenseService {

  private ExpenseRepository expenseRepository;
  private SvlcfService svlcfService;
  private CashDepositRepository cashDepositRepository;
  private DeletedTransactionRepo deletedTransactionRepo;

  /**
   * Adds a new Expense to DB
   *
   * @param expense without expense and Date
   * @return complete expense object
   */
  public Expense addExpense(Expense expense) {
    expense.setId(svlcfService.getAvailableIdByName(EXPENSE_STRING));
    expense.setDate(getCurrentDate());
    expense.setRemarks(capString(expense.getRemarks()));
    log.info("Adding new Expense {}", writeAsJson(expense));
    return expenseRepository.save(expense);
  }

  /**
   * Retrieves List of Expenses based on the Date
   *
   * @param date for retrieving the details
   * @return list of the Expenses
   */
  public List<Expense> getExpensesForTheDate(String date) {
    log.info("Getting List of Expenses for the date {}", date);
    return expenseRepository.findByDate(date);
  }

  /**
   * record a cash deposit entry into the DB
   * @param amount greater than zero
   * @return Success String
   */
  public String depositCashToAccount(Integer amount) {
    log.info("Deposit amount {} to account", amount);
    String date = getCurrentDate();
    CashDeposit updatedCacheDeposit =
        cashDepositRepository
            .findById(date)
            .map(cashDeposit -> new CashDeposit(date, cashDeposit.getCash() + amount))
            .orElse(new CashDeposit(date, amount));
    cashDepositRepository.save(updatedCacheDeposit);
    return SUCCESS;
  }

  /**
   * Gets all the cash deposits
   *
   * @return list of all cash deposits
   */
  public List<CashDeposit> getCashDeposits() {
    log.info("Getting list of cash deposits");
    return cashDepositRepository.findAll();
  }

  /**
   * Deletes the expense from DB if available or else returns error message
   *
   * @param id unique id for the expense
   * @return Success or error String
   */
  public String deleteExpenseById(Long id) {
    log.info("Delete Expense by id {}", id);
    Expense expense = expenseRepository.findById(id).orElse(null);
    if (expense == null) {
      String errorMessage = FAILED_DB_OPERATION + NO_EXPENSE_WITH_ID;
      log.info(VALUE_IN_LOG, errorMessage);
      return errorMessage;
    }
    DelTrans delTrans =
        new DelTrans(
            svlcfService.getAvailableIdByName(DEL_TRANS_STRING),
            EXPENSE_STRING,
            writeAsJson(expense));
    deletedTransactionRepo.save(delTrans);
    expenseRepository.deleteById(id);
    return SUCCESS;
  }
}
