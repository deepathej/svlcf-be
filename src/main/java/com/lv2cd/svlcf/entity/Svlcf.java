package com.lv2cd.svlcf.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Svlcf {

  @Id private String name;
  private Long availValue;
}
