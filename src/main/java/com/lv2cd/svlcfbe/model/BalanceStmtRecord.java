package com.lv2cd.svlcfbe.model;

import com.lv2cd.svlcfbe.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceStmtRecord {
  private Long userId;
  private String details;
  private PaymentMode paymentMode;
  private Integer amount;
}
