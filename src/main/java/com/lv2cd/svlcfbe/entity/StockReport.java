package com.lv2cd.svlcfbe.entity;

import static com.lv2cd.svlcfbe.util.Constants.TEXT;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StockReport {
  @Id private Long id;
  private String date;

  @Column(columnDefinition = TEXT)
  private String currentStock;
}
