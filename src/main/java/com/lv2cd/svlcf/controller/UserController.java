package com.lv2cd.svlcf.controller;

import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.entity.User;
import com.lv2cd.svlcf.enums.UserType;
import com.lv2cd.svlcf.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class UserController {

  private UserService userService;

  /**
   * API is used to create a new User in DB
   *
   * @param user object
   * @return new user including the id
   */
  @PostMapping(value = NEW_USER_ENDPOINT, consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
  public ResponseEntity<User> newUser(@RequestBody User user) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.newUser(user));
  }

  /**
   * API is used to update any field/fields of the User
   *
   * @param user object
   * @return updated user details
   */
  @PostMapping(
      value = UPDATE_USER_ENDPOINT,
      consumes = APPLICATION_JSON,
      produces = APPLICATION_JSON)
  public ResponseEntity<User> updateUser(@RequestBody User user) {
    return ResponseEntity.ok(userService.updateUser(user));
  }

  /**
   * API is used to delete the user based on the id
   *
   * @param id which is unique for the user
   * @return success string
   */
  @DeleteMapping(DELETE_USER_BY_ID_ENDPOINT)
  public ResponseEntity<String> deleteUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.deleteUser(id));
  }

  /**
   * API is used to get the list of Users based on the usertype and balance
   *
   * @param balance minimum amount
   * @param userType consumer/supplier
   * @return user list
   */
  @GetMapping(value = GET_USERS_WITH_BALANCE_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<User>> getUsersWithBalance(
      @PathVariable Integer balance, @PathVariable UserType userType) {
    return ResponseEntity.ok(userService.getUsersWithBalance(balance, userType));
  }

  /**
   * API is used to get all the users from DB
   *
   * @return user list
   */
  @GetMapping(value = GET_ALL_USERS_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<List<User>> getAllUsers() {
    return ResponseEntity.ok(userService.getAllUsers());
  }

  /**
   * API is used to get the User based on the unique id
   *
   * @param id unique for the user
   * @return user details
   */
  @GetMapping(value = GET_USER_BY_ID_ENDPOINT, produces = APPLICATION_JSON)
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getUserById(id));
  }
}
