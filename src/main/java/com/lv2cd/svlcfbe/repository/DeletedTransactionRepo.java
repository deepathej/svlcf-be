package com.lv2cd.svlcfbe.repository;

import com.lv2cd.svlcfbe.entity.DelTrans;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedTransactionRepo extends JpaRepository<DelTrans, String> {}
