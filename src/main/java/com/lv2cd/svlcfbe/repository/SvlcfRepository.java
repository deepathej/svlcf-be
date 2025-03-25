package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.Svlcf;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface SvlcfRepository extends JpaRepository<Svlcf, String> {

  String UPDATE_SVLCF_AVAILABLE_VALUE_QUERY = "update Svlcf set availValue = ?2 where name = ?1";

  @Modifying
  @Query(UPDATE_SVLCF_AVAILABLE_VALUE_QUERY)
  int updateAvailValue(String name, Long availValue);
}
