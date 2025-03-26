package com.lv2cd.svlcfbe.controller;

import static com.lv2cd.svlcfbe.enums.UserType.CONSUMER;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.lv2cd.svlcfbe.config.SecurityConfig;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(SecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private UserService userService;

  @Test
  void testNewUser() throws Exception {
    User user = getConsumerWithZeroBalanceReq();
    User user1 = getConsumerWithZeroBalance();
    when(userService.newUser(user)).thenReturn(user1);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(NEW_USER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(user)))
            .andExpect(status().isCreated())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        user1, asObjectFromString(mvcResult.getResponse().getContentAsString(), User.class));
    verify(userService, times(I_ONE)).newUser(user);
  }

  @Test
  void testUpdateUser() throws Exception {
    User user = getConsumerUpdateReq();
    User user1 = getConsumerWithBalance();
    when(userService.updateUser(user)).thenReturn(user1);
    MvcResult mvcResult =
        mockMvc
            .perform(
                post(UPDATE_USER_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(user)))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andReturn();
    assertEquals(
        user1, asObjectFromString(mvcResult.getResponse().getContentAsString(), User.class));
    verify(userService, times(I_ONE)).updateUser(user);
  }

  @Test
  void testDeleteUser() throws Exception {
    when(userService.deleteUser(L_TEN_THOUSAND)).thenReturn(SUCCESS);
    MvcResult mvcResult =
        mockMvc
            .perform(delete(DELETE_USER_BY_ID_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(SUCCESS, mvcResult.getResponse().getContentAsString());
    verify(userService, times(I_ONE)).deleteUser(L_TEN_THOUSAND);
  }

  @Test
  void testGetUsersWithBalance() throws Exception {
    User userWithBalance = getSupplierWithBalance();
    when(userService.getUsersWithBalance(I_TEN_THOUSAND, CONSUMER))
        .thenReturn(List.of(userWithBalance));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_USERS_WITH_BALANCE_ENDPOINT, I_TEN_THOUSAND, CONSUMER))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(
        I_ONE, asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(userService, times(I_ONE)).getUsersWithBalance(I_TEN_THOUSAND, CONSUMER);
  }

  @Test
  void testGetAllUsers() throws Exception {
    when(userService.getAllUsers())
        .thenReturn(
            List.of(
                getConsumerWithZeroBalance(), getSupplierWithBalance(), getConsumerWithBalance()));
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_ALL_USERS_ENDPOINT))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(
        I_THREE,
        asObjectFromString(mvcResult.getResponse().getContentAsString(), List.class).size());
    verify(userService, times(I_ONE)).getAllUsers();
  }

  @Test
  void testGetUserById() throws Exception {
    User user = getConsumerWithBalance();
    when(userService.getUserById(L_TEN_THOUSAND)).thenReturn(user);
    MvcResult mvcResult =
        mockMvc
            .perform(get(GET_USER_BY_ID_ENDPOINT, L_TEN_THOUSAND))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();
    assertEquals(
        user, asObjectFromString(mvcResult.getResponse().getContentAsString(), User.class));
    verify(userService, times(I_ONE)).getUserById(L_TEN_THOUSAND);
  }
}
