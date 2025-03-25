package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.*;
import static com.lv2cd.svlcfbe.util.CommonMethods.capString;
import static com.lv2cd.svlcfbe.util.CommonMethods.writeAsJson;
import static com.lv2cd.svlcfbe.util.Constants.*;

import com.lv2cd.svlcfbe.entity.PreBalance;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.enums.PaymentType;
import com.lv2cd.svlcfbe.enums.UserType;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.exception.CustomNotAcceptableException;
import com.lv2cd.svlcfbe.exception.CustomNotFoundException;
import com.lv2cd.svlcfbe.repository.PreBalanceRepository;
import com.lv2cd.svlcfbe.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {

  private UserRepository userRepository;
  private SvlcfService svlcfService;
  private PreBalanceRepository preBalanceRepository;

  /**
   * Creates the new User in the DB
   *
   * @param user without the id
   * @return user with id
   */
  public User newUser(User user) {
    user.setName(capString(user.getName()));
    user.setAddress(capString(user.getAddress()));
    user.setGstin(capString((user.getGstin())));
    log.info("Creating new user {}", writeAsJson(user));
    return userRepository.save(updateUserIdAndBalance(user));
  }

  /**
   * Adds the user id and the balance for the user object
   *
   * @param user without the id and balance
   * @return user object with id and balance id
   */
  private User updateUserIdAndBalance(User user) {
    log.info("Update the user id to the new user object");
    if (!userRepository
        .findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType())
        .isEmpty()) {
      throw new CustomBadRequestException(USER_ALREADY_EXISTS + COLON_SPACE + user);
    }
    Long availableId = svlcfService.getAvailableIdByName(USER_STRING);
    user.setId(availableId);
    if (user.getBalance() == null) {
      user.setBalance(I_ZERO);
    } else {
      log.info("Adding the details to Pre_balance table as the user have pending balance");
      preBalanceRepository.save(new PreBalance(user.getId(), user.getBalance()));
    }
    return user;
  }

  /**
   * Update the user details in the DB
   *
   * @param user object
   * @return complete user object
   */
  public User updateUser(User user) {
    if (isStringNotEmptyOrNull(user.getName())) {
      String name = capString(user.getName());
      user.setName(name);
      log.info("Update user name to {} for id {}", name, user.getId());
      userRepository.updateUserName(user.getId(), name);
    }
    if (isStringNotEmptyOrNull(user.getAddress())) {
      String address = capString(user.getAddress());
      user.setAddress(address);
      log.info("Update user address to {} for id {}", address, user.getId());
      userRepository.updateUserAddress(user.getId(), user.getAddress());
    }
    if (isStringNotEmptyOrNull(user.getPhoneNumber())) {
      log.info("Update user phonenumber to {} for id {}", user.getPhoneNumber(), user.getId());
      userRepository.updateUserPhoneNumber(user.getId(), user.getPhoneNumber());
    }
    if (isStringNotEmptyOrNull(user.getGstin())) {
      user.setGstin(capString(user.getGstin()));
      log.info("Update user gstin to {} for id {}", user.getGstin(), user.getId());
      userRepository.updateUserGstin(user.getId(), user.getGstin());
    }
    return getUserById(user.getId());
  }

  /**
   * Checks if the provided string is empty/null or not
   *
   * @param stringValue for checking
   * @return true/false
   */
  private boolean isStringNotEmptyOrNull(String stringValue) {
    log.info("Check if the value {} is empty or not", stringValue);
    return !(stringValue == null || stringValue.isEmpty());
  }

  /**
   * Deletes user based on the id and only if the balance is zero
   *
   * @param id unique id for User
   * @return Success message
   */
  public String deleteUser(Long id) {
    User user = getUserById(id);
    log.info("Deleting user with id {}", id);
    if (user.getBalance() > I_ZERO) {
      if (user.getUserType() == UserType.CONSUMER) {
        throw new CustomNotAcceptableException(CONSUMER_CANNOT_BE_DELETED + user.getBalance());
      } else {
        throw new CustomNotAcceptableException(SUPPLIER_CANNOT_BE_DELETED + user.getBalance());
      }
    }
    userRepository.deleteById(id);
    return SUCCESS;
  }

  /**
   * Get the User based on the Id
   *
   * @param id unique for the user
   * @return User object
   */
  public User getUserById(Long id) {
    log.info("Retrieving the User details based on id {}", id);
    return userRepository
        .findById(id)
        .orElseThrow(() -> new CustomNotFoundException(USER_NOT_REGISTERED));
  }

  /**
   * Creates a new Map with user id and the name as key and value
   *
   * @return Map with user id and name
   */
  public Map<Long, String> getUserIdAndNameMap() {
    log.info("Getting userId and name map");
    return getAllUsers().stream().collect(Collectors.toMap(User::getId, User::getName));
  }

  /**
   * Gets the list of Users with Balance higher than or equal to the provided balance
   *
   * @param balance for Query
   * @param userType consumer/supplier
   * @return list of users
   */
  public List<User> getUsersWithBalance(Integer balance, UserType userType) {
    log.info("Getting user with balance grater than {} and user type {}", balance, userType);
    return userRepository.findByBalanceGreaterThan(balance - I_ONE).stream()
        .filter(user -> user.getUserType() == userType)
        .sorted(Comparator.comparing(User::getBalance).reversed())
        .toList();
  }

  /**
   * update the user balance
   *
   * @param id unique id for User
   * @param amount paid
   * @param balance current balance
   * @param paymentType sales/purchase
   * @return unpated balance
   */
  public Integer updateUserBalance(
      Long id, Integer amount, Integer balance, PaymentType paymentType) {
    if (amount <= balance && paymentType == PAYMENT) {
      balance -= amount;
    } else if (paymentType == SALES || paymentType == PURCHASE) {
      balance += amount;
    } else {
      throw new CustomBadRequestException(BALANCE_UPDATE_FAILED + balance);
    }
    log.info(
        "Updating the user balance to {} for userId {} and payment type is {}",
        balance,
        id,
        paymentType);
    if (userRepository.updateBalanceForATransaction(id, balance) != I_ONE) {
      throw new CustomInternalServerException(FAILED_DB_OPERATION + USER_STRING);
    }
    return balance;
  }

  /**
   * Updates the user balance
   *
   * @param id unique id for the User
   * @param balance new balance
   */
  public void directUpdateUserBalance(Long id, Integer balance) {
    log.info("update user balance directly to {} for userid {}", balance, id);
    if (userRepository.updateBalanceForATransaction(id, balance) != I_ONE) {
      throw new CustomInternalServerException(FAILED_DB_OPERATION + USER_STRING);
    }
  }

  /**
   * Get list of all Users
   *
   * @return list of users
   */
  public List<User> getAllUsers() {
    log.info("Getting List of All Users");
    return userRepository.findAll().stream().sorted(Comparator.comparing(User::getName)).toList();
  }
}
