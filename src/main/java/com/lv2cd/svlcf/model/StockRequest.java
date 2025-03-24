package com.lv2cd.svlcf.model;

import com.lv2cd.svlcf.entity.Stock;
import lombok.Data;

@Data
public class StockRequest {
  private Stock stock;
  private Long userId;
}
