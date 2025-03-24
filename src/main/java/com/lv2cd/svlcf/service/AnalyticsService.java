package com.lv2cd.svlcf.service;

import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.ValidStatus;
import com.lv2cd.svlcf.model.DataHealthGraph;
import com.lv2cd.svlcf.repository.ValidStatusRepo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AnalyticsService {

  private ValidStatusRepo validStatusRepo;

  /**
   * creates the list with type and the status color
   *
   * @return list of validStatus objects
   */
  public DataHealthGraph getDataHealthGraph() {
    List<ValidStatus> validStatusList = validStatusRepo.findAll();
    log.info("Get data health details {}", validStatusList);
    Map<String, String> statusMap =
        validStatusList.stream()
            .collect(
                Collectors.toMap(
                    ValidStatus::getName,
                    ValidStatus::getStatus,
                    (existing, replacement) -> existing));
    return DataHealthGraph.builder()
        .userData(convertValidStatusToDataHealthGraph(statusMap, USER_DATA))
        .dataBaseBackup(convertValidStatusToDataHealthGraph(statusMap, DATA_BASE_BACKUP))
        .stockReport(convertValidStatusToDataHealthGraph(statusMap, STOCK_REPORT))
        .build();
  }

  /**
   * @param statusMap stores the status details
   * @param value passed/Failed
   * @return red/green
   */
  private String convertValidStatusToDataHealthGraph(Map<String, String> statusMap, String value) {
    log.info("Converting the value {}", value);
    return statusMap.getOrDefault(value, FAILED).equalsIgnoreCase(FAILED) ? RED : GREEN;
  }
}
