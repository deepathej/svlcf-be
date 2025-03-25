package com.lv2cd.svlcfbe.model;

import com.lv2cd.svlcfbe.entity.Stock;
import com.lv2cd.svlcfbe.entity.User;
import java.util.List;
import lombok.Data;

@Data
public class SalesRequest {
  private List<Stock> saleProducts;
  private User user;
}
