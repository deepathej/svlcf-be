package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.User;
import com.lv2cd.svlcfbe.enums.UserType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface UserRepository extends JpaRepository<User, Long> {

  String UPDATE_USER_PHONE_NUMBER_QUERY = "update User set phoneNumber = ?2 where id = ?1";
  String UPDATE_USER_GSTIN_QUERY = "update User set gstin = ?2 where id = ?1";
  String UPDATE_USER_NAME_QUERY = "update User set name = ?2 where id = ?1";
  String UPDATE_USER_ADDRESS_QUERY = "update User set address = ?2 where id = ?1";
  String UPDATE_USER_BALANCE_QUERY = "update User set balance = ?2 where id = ?1";

  List<User> findByNameAndPhoneNumberAndUserType(
      String name, String phoneNumber, UserType userType);

  List<User> findByBalanceGreaterThan(Integer balance);

  @Modifying
  @Query(UPDATE_USER_BALANCE_QUERY)
  int updateBalanceForATransaction(Long id, Integer balance);

  @Modifying
  @Query(UPDATE_USER_NAME_QUERY)
  void updateUserName(Long id, String name);

  @Modifying
  @Query(UPDATE_USER_ADDRESS_QUERY)
  void updateUserAddress(Long id, String address);

  @Modifying
  @Query(UPDATE_USER_GSTIN_QUERY)
  void updateUserGstin(Long id, String gstin);

  @Modifying
  @Query(UPDATE_USER_PHONE_NUMBER_QUERY)
  void updateUserPhoneNumber(Long id, String phoneNumber);
}
