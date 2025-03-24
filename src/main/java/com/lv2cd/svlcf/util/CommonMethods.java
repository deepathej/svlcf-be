package com.lv2cd.svlcf.util;

import static com.lv2cd.svlcf.util.Constants.DATE_FORMAT;
import static com.lv2cd.svlcf.util.Constants.TIME_FORMAT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lv2cd.svlcf.exception.CustomInternalServerException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonMethods {

  private CommonMethods() {}

  public static String capString(String data) {
    return data.toUpperCase(Locale.ROOT);
  }

  public static String getCurrentTime() {
    log.info("Getting current time");
    return LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT));
  }

  public static String getCurrentDate() {
    log.info("Getting current date");
    return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
  }

  public static <T> String writeAsJson(T valueType) {
    log.info("Write String as Json");
    try {
      return new ObjectMapper().writeValueAsString(valueType);
    } catch (JsonProcessingException e) {
      throw new CustomInternalServerException(e.getMessage());
    }
  }
}
