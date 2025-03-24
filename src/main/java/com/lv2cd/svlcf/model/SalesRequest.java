package com.lv2cd.svlcf.model;

import com.lv2cd.svlcf.entity.Stock;
import com.lv2cd.svlcf.entity.User;
import java.util.List;
import lombok.Data;

@Data
public class SalesRequest {
  private List<Stock> saleProducts;
  private User user;
}
