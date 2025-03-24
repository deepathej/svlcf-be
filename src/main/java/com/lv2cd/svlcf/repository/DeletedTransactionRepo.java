package com.lv2cd.svlcf.repository;

import com.lv2cd.svlcf.entity.DelTrans;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedTransactionRepo extends JpaRepository<DelTrans, String> {}
