package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.util.CommonTestMethods.getDataHealthGraph;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.ValidStatus;
import com.lv2cd.svlcfbe.model.DataHealthGraph;
import com.lv2cd.svlcfbe.repository.ValidStatusRepo;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

  @Mock private ValidStatusRepo validStatusRepo;
  @InjectMocks private AnalyticsService analyticsService;

  @Test
  void testGetDataHealthGraph() {
    when(validStatusRepo.findAll()).thenReturn(List.of(new ValidStatus(USER_DATA, BLANK_STRING),
            new ValidStatus(DATA_BASE_BACKUP, FAILED), new ValidStatus(STOCK_REPORT_STRING, BLANK_STRING), new ValidStatus(STOCK_REPORT_STRING, BLANK_STRING)));
    DataHealthGraph dataHealthGraph = analyticsService.getDataHealthGraph();
    assertEquals(getDataHealthGraph(), dataHealthGraph);
  }
}
