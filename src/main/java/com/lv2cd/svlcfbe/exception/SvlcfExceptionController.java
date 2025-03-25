package com.lv2cd.svlcfbe.exception;

import com.lv2cd.svlcfbe.model.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class SvlcfExceptionController {

  @ExceptionHandler(CustomBadRequestException.class)
  public ResponseEntity<ErrorMessage> customBadRequestException(
      CustomBadRequestException exception) {
    String message = exception.getMessage();
    log.error(message);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessage(message));
  }

  @ExceptionHandler(CustomNotFoundException.class)
  public ResponseEntity<ErrorMessage> customNotFoundException(CustomNotFoundException exception) {
    String message = exception.getMessage();
    log.error(message);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(message));
  }

  @ExceptionHandler(CustomNotAcceptableException.class)
  public ResponseEntity<ErrorMessage> customNotAcceptableException(
      CustomNotAcceptableException exception) {
    String message = exception.getMessage();
    log.error(message);
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(new ErrorMessage(message));
  }

  @ExceptionHandler(CustomInternalServerException.class)
  public ResponseEntity<ErrorMessage> customInternalServerException(
      CustomInternalServerException exception) {
    String message = exception.getMessage();
    log.error(message);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessage(message));
  }
}
