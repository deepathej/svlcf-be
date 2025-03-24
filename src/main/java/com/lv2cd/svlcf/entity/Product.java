package com.lv2cd.svlcf.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id private Long id;
  private Long userId;
  private Long invoiceNumber;
  private String item;
  private Integer quantity;
  private Integer variant;
  private Integer price;
  private Integer amount;
  private String date;
}
