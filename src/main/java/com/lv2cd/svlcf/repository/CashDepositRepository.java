package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.CashDeposit;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface CashDepositRepository extends JpaRepository<CashDeposit, String> {}
