package com.lv2cd.svlcfbe.entity;

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
