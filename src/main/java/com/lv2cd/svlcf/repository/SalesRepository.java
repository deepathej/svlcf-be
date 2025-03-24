package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.Sales;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SalesRepository extends JpaRepository<Sales, Long> {

  String UPDATE_SALES_USERID_WITH_TECH_USERID =
      "update Sales set userId = ?2 , saleAmount = ?3 , userUpdatedBalance = ?4 where invoiceNumber = ?1";

  List<Sales> findByDate(String date);

  @Modifying
  @Query(UPDATE_SALES_USERID_WITH_TECH_USERID)
  void updateTechDetailsForSale(
      Long invoiceNumber, Long userId, Integer saleAmount, Integer userUpdatedBalance);
}
