package com.lv2cd.svlcf.controller;

import com.lv2cd.svlcf.model.DataHealthGraph;
import com.lv2cd.svlcf.service.AnalyticsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lv2cd.svlcf.util.Constants.APPLICATION_JSON;
import static com.lv2cd.svlcf.util.Constants.DATA_HEALTH_GRAPH_ENDPOINT;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class AnalyticsController {

  private AnalyticsService analyticsService;

  /**
   * API is used to return all the details needed for the Data health graph with color code. So that
   * when it is displayed in the FE. So that it is easier to identify the Data health issues
   *
   * @return DataHealthGroup object
   */
  @GetMapping(value = DATA_HEALTH_GRAPH_ENDPOINT, produces = APPLICATION_JSON)
  public DataHealthGraph getDataHealthGraph() {
    return analyticsService.getDataHealthGraph();
  }
}
