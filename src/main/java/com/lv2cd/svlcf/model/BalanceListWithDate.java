package com.lv2cd.svlcf.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BalanceListWithDate {
  private List<BalanceStmtRecord> inAmountRecords;
  private List<BalanceStmtRecord> outAmountRecords;
  private String date;
}
