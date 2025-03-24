package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.Svlcf;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import com.lv2cd.svlcf.repository.SvlcfRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class SvlcfService {

  private SvlcfRepository svlcfRepository;

  public Long getAvailableIdByName(String name) {
    Long availId = getOnlyAvailableIdByName(name);
    updateAvailIdByName(name, availId + L_ONE);
    return availId;
  }

  public Long getOnlyAvailableIdByName(String name) {
    log.info("Get available id for {}", name);
    return svlcfRepository
        .findById(name)
        .map(Svlcf::getAvailValue)
        .orElseThrow(() -> new CustomInternalServerException(FAILED_DB_OPERATION + SVLCF_STRING));
  }

  public void updateAvailIdByName(String name, Long availId) {
    log.info("Update available id to {} for {}", availId, name);
    int response = svlcfRepository.updateAvailValue(name, availId);
    if (response != I_ONE) {
      throw new CustomInternalServerException(SEQUENCE_VALUE_UPDATE_FAILED + availId);
    }
  }
}
