package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface ProductRepository extends JpaRepository<Product, Long> {

  String UPDATE_PRODUCT_WITH_TECH_USER_DETAILS =
      "update Product set userId = ?2 , quantity = ?3 , amount = ?4 where id = ?1";

  List<Product> findByDate(String date);

  List<Product> getProductByUserId(Long userId);

  @Modifying
  @Query(UPDATE_PRODUCT_WITH_TECH_USER_DETAILS)
  void updateTechDetailsForProduct(Long productId, Long userId, Integer quantity, Integer amount);
}
