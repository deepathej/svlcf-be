package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.getSvlcf;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.repository.SvlcfRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SvlcfServiceTest {

  @Mock private SvlcfRepository svlcfRepository;
  @InjectMocks private SvlcfService svlcfService;

  @Test
  void testGetAvailableIdByNameMethodWhenRecordAlreadyExistsInDBThenUpdateRecordInDB() {
    when(svlcfRepository.findById(USER_STRING)).thenReturn(Optional.of(getSvlcf()));
    when(svlcfRepository.updateAvailValue(USER_STRING, L_TEN_THOUSAND + L_ONE)).thenReturn(I_ONE);
    Long actualValue = svlcfService.getAvailableIdByName(USER_STRING);
    assertEquals(L_TEN_THOUSAND, actualValue);
  }

  @Test
  void testGetAvailableIdByNameMethodWhenRecordNotInDBThenExceptionIsThrown() {
    when(svlcfRepository.findById(USER_STRING)).thenReturn(Optional.empty());
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> svlcfService.getAvailableIdByName(USER_STRING),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + SVLCF_STRING, thrown.getMessage());
  }

  @Test
  void testGetAvailableIdByNameMethodWhenUpdateRecordFailedThenExceptionIsThrown() {
    Long expectedNextValue = L_TEN_THOUSAND + L_ONE;
    when(svlcfRepository.findById(USER_STRING)).thenReturn(Optional.of(getSvlcf()));
    when(svlcfRepository.updateAvailValue(USER_STRING, expectedNextValue)).thenReturn(I_ZERO);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> svlcfService.getAvailableIdByName(USER_STRING),
            EXCEPTION_NOT_THROWN);
    assertEquals(SEQUENCE_VALUE_UPDATE_FAILED + expectedNextValue, thrown.getMessage());
  }
}
