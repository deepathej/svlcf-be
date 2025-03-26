package com.lv2cd.svlcfbe.exception;

import static com.lv2cd.svlcfbe.util.TestConstants.CUSTOM_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lv2cd.svlcfbe.model.ErrorMessage;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SvlcfExceptionControllerTest {

  @InjectMocks private SvlcfExceptionController svlcfExceptionController;

  @Test
  void testCustomBadRequestException() {
    ResponseEntity<ErrorMessage> responseEntity =
        svlcfExceptionController.customBadRequestException(
            new CustomBadRequestException(CUSTOM_EXCEPTION));
    assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    assertEquals(CUSTOM_EXCEPTION, Objects.requireNonNull(responseEntity.getBody()).getMessage());
  }

  @Test
  void testCustomNotFoundException() {
    ResponseEntity<ErrorMessage> responseEntity =
        svlcfExceptionController.customNotFoundException(
            new CustomNotFoundException(CUSTOM_EXCEPTION));
    assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    assertEquals(CUSTOM_EXCEPTION, Objects.requireNonNull(responseEntity.getBody()).getMessage());
  }

  @Test
  void testCustomInternalServerException() {
    ResponseEntity<ErrorMessage> responseEntity =
        svlcfExceptionController.customInternalServerException(
            new CustomInternalServerException(CUSTOM_EXCEPTION));
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertEquals(CUSTOM_EXCEPTION, Objects.requireNonNull(responseEntity.getBody()).getMessage());
  }

  @Test
  void testCustomNotAcceptableException() {
    ResponseEntity<ErrorMessage> responseEntity =
        svlcfExceptionController.customNotAcceptableException(
            new CustomNotAcceptableException(CUSTOM_EXCEPTION));
    assertEquals(HttpStatus.NOT_ACCEPTABLE, responseEntity.getStatusCode());
    assertEquals(CUSTOM_EXCEPTION, Objects.requireNonNull(responseEntity.getBody()).getMessage());
  }
}
