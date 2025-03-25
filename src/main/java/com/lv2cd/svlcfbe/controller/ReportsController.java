package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.model.ReportsRequest;
import com.lv2cd.svlcfbe.service.ReportsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class ReportsController {

  private ReportsService reportsService;

  /**
   * API is used to generate the dailyReports based on the option selected by the user
   *
   * @param reportsRequest with the type and date
   * @return pdf path
   */
  @PostMapping(value = REPORTS_ENDPOINT, consumes = APPLICATION_JSON)
  public ResponseEntity<String> reports(@RequestBody ReportsRequest reportsRequest) {
    return ResponseEntity.ok(reportsService.reports(reportsRequest));
  }

  /**
   * API is used to generate the user report based on the id provided
   *
   * @param id unique id for the user
   * @return success message
   */
  @GetMapping(value = GET_USER_REPORT_ENDPOINT)
  public ResponseEntity<String> getUserReport(@PathVariable Long id) {
    return ResponseEntity.ok(reportsService.getUserReport(id, false));
  }

  /**
   * API is used to generate reports for all the users
   *
   * @return success message
   */
  @GetMapping(value = GET_ALL_USER_REPORT_ENDPOINT)
  public ResponseEntity<String> getAllUserReports() {
    return ResponseEntity.ok(reportsService.getAllUserReports());
  }

  /**
   * API is used to validate all the stock reports from the beginning to till date
   *
   * @return success message
   */
  @GetMapping(value = VALIDATE_STOCK_REPORTS)
  public ResponseEntity<String> validateStockReports() {
    return ResponseEntity.ok(reportsService.validateStockReports());
  }

  /**
   * API is used to create the basic directory structure needed for this service to generate the
   * necessary details by the service
   *
   * @return success message
   */
  @GetMapping(value = CREATE_DIRECTORIES)
  public ResponseEntity<String> createDirectories() {
    return ResponseEntity.ok(reportsService.createDirectories());
  }
}
