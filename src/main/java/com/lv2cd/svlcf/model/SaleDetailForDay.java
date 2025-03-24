package com.lv2cd.svlcf.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleDetailForDay {
  private Long invoiceNumber;
  private Long userId;
  private String name;
  private Integer saleAmount;
  private String itemDetails;
}
