package com.lv2cd.svlcfbe.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Sales {
  @Id private Long invoiceNumber;
  private String productIds;
  private Long userId;
  private Integer saleAmount;
  private Integer userUpdatedBalance;
  private String date;
}
