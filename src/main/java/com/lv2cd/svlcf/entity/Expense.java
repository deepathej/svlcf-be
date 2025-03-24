package com.lv2cd.svlcf.entity;

import com.lv2cd.svlcf.enums.PaymentMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Expense {
  @Id private Long id;
  private Integer amount;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  private String remarks;
  private String date;
}
