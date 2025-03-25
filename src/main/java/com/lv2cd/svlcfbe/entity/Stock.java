package com.lv2cd.svlcfbe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Stock {
  @Id private Long id;
  private String name;
  private String brand;
  private Integer price;
  private Integer quantity;
  private Integer variant;
}
