package com.lv2cd.svlcfbe.model;

import com.lv2cd.svlcfbe.entity.Stock;
import lombok.Data;

@Data
public class StockRequest {
  private Stock stock;
  private Long userId;
}
