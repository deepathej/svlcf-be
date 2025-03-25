package com.lv2cd.svlcfbe.model;

import lombok.*;

@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DataHealthGraph {
  private String dataBaseBackup;
  private String stockReport;
  private String userData;
}
