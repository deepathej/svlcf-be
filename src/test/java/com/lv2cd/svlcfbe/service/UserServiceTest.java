package com.lv2cd.svlcfbe.service;

import static com.lv2cd.svlcfbe.enums.PaymentType.PAYMENT;
import static com.lv2cd.svlcfbe.enums.UserType.CONSUMER;
import static com.lv2cd.svlcfbe.util.CommonTestMethods.*;
import static com.lv2cd.svlcfbe.util.Constants.*;
import static com.lv2cd.svlcfbe.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lv2cd.svlcfbe.entity.PreBalance;
import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.enums.PaymentType;
import com.lv2cd.svlcfbe.exception.CustomBadRequestException;
import com.lv2cd.svlcfbe.exception.CustomInternalServerException;
import com.lv2cd.svlcfbe.exception.CustomNotAcceptableException;
import com.lv2cd.svlcfbe.exception.CustomNotFoundException;
import com.lv2cd.svlcfbe.repository.PreBalanceRepository;
import com.lv2cd.svlcfbe.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private SvlcfService svlcfService;
  @Mock private PreBalanceRepository preBalanceRepository;
  @InjectMocks private UserService userService;

  @Test
  void testNewUserMethodWhenConsumerWithoutBalanceThenNewUserIsCreatedWithZeroBalance() {
    User user = getConsumerWithZeroBalanceReq();
    User expectedUser = getConsumerWithZeroBalance();
    when(userRepository.findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType()))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(USER_STRING)).thenReturn(L_TEN_THOUSAND);
    when(userRepository.save(expectedUser)).thenReturn(expectedUser);
    User actualUser = userService.newUser(user);
    assertEquals(expectedUser, actualUser);
  }

  @Test
  void testNewUserMethodWhenConsumerWithBalanceThenNewUserIsCreatedWithSpecifiedBalance() {
    User user = getConsumerWithBalanceReq();
    User expectedUser = getConsumerWithBalance();
    when(userRepository.findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType()))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(USER_STRING)).thenReturn(L_TEN_THOUSAND_AND_ONE);
    when(userRepository.save(expectedUser)).thenReturn(expectedUser);
    PreBalance preBalance = new PreBalance(L_TEN_THOUSAND_AND_ONE, user.getBalance());
    when(preBalanceRepository.save(preBalance)).thenReturn(preBalance);
    User actualUser = userService.newUser(user);
    assertEquals(expectedUser, actualUser);
  }

  @Test
  void testNewUserMethodWhenSupplierWithoutBalanceThenNewUserIsCreatedWithZeroBalance() {
    User user = getSupplierWithZeroBalanceReq();
    User expectedUser = getSupplierWithZeroBalance();
    when(userRepository.findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType()))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(USER_STRING)).thenReturn(L_TEN_THOUSAND_AND_TWO);
    when(userRepository.save(expectedUser)).thenReturn(expectedUser);
    User actualUser = userService.newUser(user);
    assertEquals(expectedUser, actualUser);
  }

  @Test
  void testNewUserMethodWhenSupplierWithBalanceThenNewUserIsCreatedWithSpecifiedBalance() {
    User user = getSupplierWithBalanceReq();
    User expectedUser = getSupplierWithBalance();
    when(userRepository.findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType()))
        .thenReturn(List.of());
    when(svlcfService.getAvailableIdByName(USER_STRING)).thenReturn(L_TEN_THOUSAND_AND_THREE);
    when(userRepository.save(expectedUser)).thenReturn(expectedUser);
    PreBalance preBalance = new PreBalance(L_TEN_THOUSAND_AND_THREE, user.getBalance());
    when(preBalanceRepository.save(preBalance)).thenReturn(preBalance);
    User actualUser = userService.newUser(user);
    assertEquals(expectedUser, actualUser);
  }

  @Test
  void testNewUserMethodWhenUserAlreadyInDBThenExceptionIsThrown() {
    User user = getConsumerWithZeroBalanceReq();
    when(userRepository.findByNameAndPhoneNumberAndUserType(
            user.getName(), user.getPhoneNumber(), user.getUserType()))
        .thenReturn(List.of(getConsumerWithZeroBalance()));
    Exception exception =
        assertThrows(
            CustomBadRequestException.class, () -> userService.newUser(user), EXCEPTION_NOT_THROWN);
    assertEquals(USER_ALREADY_EXISTS + COLON_SPACE + user, exception.getMessage());
  }

  @Test
  void testUpdateUserMethodWhenEmptyDetailsAreSentThenNothingIsUpdated() {
    User user = getConsumerUpdateEmptyReq();
    when(userRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(user));
    User actualUser = userService.updateUser(user);
    assertEquals(user, actualUser);
  }

  @Test
  void testUpdateUserMethodWhenNullDetailsAreSentThenNothingIsUpdated() {
    User user = getConsumerUpdateNullReq();
    when(userRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(user));
    User actualUser = userService.updateUser(user);
    assertEquals(user, actualUser);
  }

  @Test
  void testUpdateUserMethod() {
    User user = getConsumerWithBalance();
    when(userRepository.findById(L_TEN_THOUSAND_AND_ONE)).thenReturn(Optional.of(user));
    doNothing().when(userRepository).updateUserName(L_TEN_THOUSAND_AND_ONE, user.getName());
    doNothing().when(userRepository).updateUserAddress(L_TEN_THOUSAND_AND_ONE, user.getAddress());
    doNothing().when(userRepository).updateUserPhoneNumber(L_TEN_THOUSAND_AND_ONE, user.getPhoneNumber());
    doNothing().when(userRepository).updateUserGstin(L_TEN_THOUSAND_AND_ONE, user.getGstin());
    User actualUser = userService.updateUser(user);
    assertEquals(user, actualUser);
  }

  @Test
  void testDeleteUserMethodWhenUserNotInDBThenExceptionIsThrown() {
    when(userRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception exception =
        assertThrows(
            CustomNotFoundException.class,
            () -> userService.deleteUser(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(USER_NOT_REGISTERED, exception.getMessage());
  }

  @Test
  void testDeleteUserMethodWhenValidConsumerIdButBalanceIsMoreThanZeroThenUserDeletedFromDB() {
    User user = getConsumerWithBalance();
    when(userRepository.findById(L_TEN_THOUSAND_AND_ONE)).thenReturn(Optional.of(user));
    Exception thrown =
        assertThrows(
            CustomNotAcceptableException.class,
            () -> userService.deleteUser(L_TEN_THOUSAND_AND_ONE),
            EXCEPTION_NOT_THROWN);
    assertEquals(thrown.getMessage(), CONSUMER_CANNOT_BE_DELETED + user.getBalance());
  }

  @Test
  void testDeleteUserMethodWhenValidSupplierIdButBalanceIsMoreThanZeroThenUserDeletedFromDB() {
    User user = getSupplierWithBalance();
    when(userRepository.findById(L_TEN_THOUSAND_AND_THREE)).thenReturn(Optional.of(user));
    Exception thrown =
        assertThrows(
            CustomNotAcceptableException.class,
            () -> userService.deleteUser(L_TEN_THOUSAND_AND_THREE),
            EXCEPTION_NOT_THROWN);
    assertEquals(thrown.getMessage(), SUPPLIER_CANNOT_BE_DELETED + user.getBalance());
  }

  @Test
  void testDeleteUserMethodWhenValidUserIdAndBalanceIsZeroThenUserDeletedFromDB() {
    User user = getConsumerWithZeroBalance();
    when(userRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.of(user));
    doNothing().when(userRepository).deleteById(L_TEN_THOUSAND);
    assertEquals(SUCCESS, userService.deleteUser(L_TEN_THOUSAND));
    verify(userRepository, times(I_ONE)).deleteById(L_TEN_THOUSAND);
    verify(userRepository, times(I_ONE)).findById(L_TEN_THOUSAND);
  }

  @Test
  void testGetUserByIdMethodWhenUserIdNotInDBThenExceptionIsThrown() {
    when(userRepository.findById(L_TEN_THOUSAND)).thenReturn(Optional.empty());
    Exception exception =
        assertThrows(
            CustomNotFoundException.class,
            () -> userService.getUserById(L_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(USER_NOT_REGISTERED, exception.getMessage());
  }

  @Test
  void testGetUserByIdMethodWhenValidUserIdIsSentThenUserDetailsIsRetrieved() {
    User user = getSupplierWithBalance();
    when(userRepository.findById(L_TEN_THOUSAND_AND_THREE)).thenReturn(Optional.of(user));
    assertEquals(user, userService.getUserById(L_TEN_THOUSAND_AND_THREE));
  }

  @Test
  void testGetUsersWithBalanceMethodWhenValidBalanceIsSentThenUserWithBalanceIsRetrieved() {
    when(userRepository.findByBalanceGreaterThan(9999))
        .thenReturn(List.of(getConsumerWithZeroBalance(), getSupplierWithBalance()));
    List<User> userList = userService.getUsersWithBalance(I_TEN_THOUSAND, CONSUMER);
    assertEquals(I_ONE, userList.size());
  }

  @Test
  void testUpdateBalanceMethodWhenBalanceLessThanAmountThenExceptionIsThrown() {
    Exception thrown =
        assertThrows(
            CustomBadRequestException.class,
            () ->
                userService.updateUserBalance(
                    L_TEN_THOUSAND, I_TWO_THOUSAND, I_THREE_HUNDRED, PAYMENT),
            EXCEPTION_NOT_THROWN);
    assertEquals(BALANCE_UPDATE_FAILED + I_THREE_HUNDRED, thrown.getMessage());
  }

  @Test
  void testUpdateBalanceMethodWhenDBUpdateFailedThenExceptionIsThrown() {
    when(userRepository.updateBalanceForATransaction(L_TEN_THOUSAND, I_THOUSAND_SEVEN_HUNDRED))
        .thenReturn(I_ZERO);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () ->
                userService.updateUserBalance(
                    L_TEN_THOUSAND, I_THREE_HUNDRED, I_TWO_THOUSAND, PAYMENT),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + USER_STRING, thrown.getMessage());
  }

  @ParameterizedTest
  @EnumSource(PaymentType.class)
  void testUpdateBalanceForATransactionMethodWithAllPaymentType(PaymentType paymentType) {
    if (PAYMENT == paymentType) {
      when(userRepository.updateBalanceForATransaction(L_TEN_THOUSAND, I_THOUSAND_SEVEN_HUNDRED))
          .thenReturn(I_ONE);
    } else {
      when(userRepository.updateBalanceForATransaction(
              L_TEN_THOUSAND, I_TWO_THOUSAND_THREE_HUNDRED))
          .thenReturn(I_ONE);
    }
    Integer actualBalance =
        userService.updateUserBalance(L_TEN_THOUSAND, I_THREE_HUNDRED, I_TWO_THOUSAND, paymentType);
    if (PAYMENT == paymentType) {
      assertEquals(I_THOUSAND_SEVEN_HUNDRED, actualBalance);
    } else {
      assertEquals(I_TWO_THOUSAND_THREE_HUNDRED, actualBalance);
    }
  }

  @Test
  void testGetAllUsersMethod() {
    when(userRepository.findAll())
        .thenReturn(
            Arrays.asList(
                getConsumerWithZeroBalance(), getConsumerWithBalance(), getSupplierWithBalance()));
    List<User> excectedUserList = userService.getAllUsers();
    assertEquals(I_THREE, excectedUserList.size());
  }

  @Test
  void testGetUserIdAndNameMap() {
    when(userRepository.findAll())
        .thenReturn(
            Arrays.asList(
                getConsumerWithZeroBalance(), getConsumerWithBalance(), getSupplierWithBalance()));
    Map<Long, String> expectedMap = userService.getUserIdAndNameMap();
    assertEquals(I_THREE, expectedMap.size());
  }

  @Test
  void testDirectUpdateUserBalanceMethodWhenDBOperationFailedThenExceptionIsThrown() {
    when(userRepository.updateBalanceForATransaction(L_TEN_THOUSAND, I_TEN_THOUSAND))
        .thenReturn(I_ZERO);
    Exception thrown =
        assertThrows(
            CustomInternalServerException.class,
            () -> userService.directUpdateUserBalance(L_TEN_THOUSAND, I_TEN_THOUSAND),
            EXCEPTION_NOT_THROWN);
    assertEquals(FAILED_DB_OPERATION + USER_STRING, thrown.getMessage());
  }

  @Test
  void testDirectUpdateUserBalanceMethodWhenValidDataSentThenValidResponseSentBack() {
    when(userRepository.updateBalanceForATransaction(L_TEN_THOUSAND, I_TEN_THOUSAND))
        .thenReturn(I_ONE);
    userService.directUpdateUserBalance(L_TEN_THOUSAND, I_TEN_THOUSAND);
    verify(userRepository, times(I_ONE))
        .updateBalanceForATransaction(L_TEN_THOUSAND, I_TEN_THOUSAND);
  }
}
