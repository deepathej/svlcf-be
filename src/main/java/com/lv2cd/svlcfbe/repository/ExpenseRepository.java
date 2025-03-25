package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.Expense;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  List<Expense> findByDate(String date);
}
