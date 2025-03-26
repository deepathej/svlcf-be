package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.service.DbBackupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(DbBackupController.class)
class DbBackupControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private DbBackupService dbBackupService;

  @Test
  void testBackUpDBData() throws Exception {
    when(dbBackupService.backUpDBData()).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(get(DB_BACKUP_ENDPOINT))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(dbBackupService, times(I_ONE)).backUpDBData();
  }
}
