package com.lv2cd.svlcf.entity;

import static com.lv2cd.svlcf.util.Constants.TEXT;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DelTrans {

  @Id private Long id;
  private String type;

  @Column(columnDefinition = TEXT)
  private String data;
}
