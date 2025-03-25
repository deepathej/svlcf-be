package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.StockReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReportRepository extends JpaRepository<StockReport, Long> {
  List<StockReport> findByDate(String date);
}
