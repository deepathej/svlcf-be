package com.lv2cd.svlcf.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SalesListWithDate {
  private List<SaleDetailForDay> saleDetailForDayList;
  private String date;
}
