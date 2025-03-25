package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByDate(String date);

  List<Payment> findByUserId(Long userId);
}
