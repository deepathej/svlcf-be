package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  List<Expense> findByDate(String date);
}
