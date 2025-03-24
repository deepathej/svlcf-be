package com.lv2cd.svlcf.entity;

import com.lv2cd.svlcf.enums.PaymentMode;
import com.lv2cd.svlcf.enums.PaymentType;
import com.lv2cd.svlcf.enums.UserType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Payment {
  @Id private Long id;
  private Long userId;

  @Enumerated(EnumType.STRING)
  private UserType userType;

  @Enumerated(EnumType.STRING)
  private PaymentMode paymentMode;

  @Enumerated(EnumType.STRING)
  private PaymentType paymentType;

  private Integer previousBalance;
  private Integer paymentAmount;
  private Integer balanceAmount;
  private String date;
  private String time;
}
