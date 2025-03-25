package com.lv2cd.svlcfbe.entity;

import com.lv2cd.svlcfbe.enums.UserType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Entity
@NoArgsConstructor
public class User {
  @Id private Long id;
  private String name;
  private String address;
  private String phoneNumber;
  private Integer balance;

  @Enumerated(EnumType.STRING)
  private UserType userType;

  private String gstin;
}
