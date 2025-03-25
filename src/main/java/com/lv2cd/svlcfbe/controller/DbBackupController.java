package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.service.DbBackupService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class DbBackupController {

  private DbBackupService dbBackupService;

  /**
   * API is used to back-up all the data from DB as SQL queries. so that the data can be used in
   * case of any failures to the DB
   *
   * @return sql queries with Data from DB
   */
  @GetMapping(value = DB_BACKUP_ENDPOINT)
  public ResponseEntity<String> backUpDBData() {
    return ResponseEntity.ok(dbBackupService.backUpDBData());
  }
}
