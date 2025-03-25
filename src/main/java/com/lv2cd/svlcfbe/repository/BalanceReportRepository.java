package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.BalanceReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceReportRepository extends JpaRepository<BalanceReport, Long> {
  List<BalanceReport> findByDate(String date);
}
