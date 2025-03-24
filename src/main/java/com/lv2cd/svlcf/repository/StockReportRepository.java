package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.StockReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReportRepository extends JpaRepository<StockReport, Long> {
  List<StockReport> findByDate(String date);
}
