package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.Stock;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface StockRepository extends JpaRepository<Stock, Long> {
  String UPDATE_STOCK_PRICE_QUERY = "update Stock set price = ?2 where id = ?1";
  String UPDATE_STOCK_QUANTITY_QUERY = "update Stock set quantity = ?2 where id = ?1";

  List<Stock> findByNameAndBrandAndVariant(String name, String brand, Integer variant);

  @Modifying
  @Query(UPDATE_STOCK_PRICE_QUERY)
  int updatePriceForStock(Long id, Integer price);

  @Modifying
  @Query(UPDATE_STOCK_QUANTITY_QUERY)
  int updateQuantityOfStock(Long id, Integer quantity);
}
