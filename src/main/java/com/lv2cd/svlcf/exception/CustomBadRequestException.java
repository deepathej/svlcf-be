package com.lv2cd.svlcf.exception;

public class CustomBadRequestException extends RuntimeException {

  public CustomBadRequestException(String message) {
    super(message);
  }
}
