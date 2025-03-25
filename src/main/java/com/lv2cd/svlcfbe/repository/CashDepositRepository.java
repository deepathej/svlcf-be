package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.CashDeposit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface CashDepositRepository extends JpaRepository<CashDeposit, String> {}
