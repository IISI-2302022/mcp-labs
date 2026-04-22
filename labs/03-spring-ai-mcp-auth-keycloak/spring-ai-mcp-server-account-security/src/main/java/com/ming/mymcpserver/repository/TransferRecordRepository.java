package com.ming.mymcpserver.repository;

import com.ming.mymcpserver.model.TransferRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {
    List<TransferRecord> findByFromAccountOrToAccountOrderByTransferTimeDesc(String fromAccount, String toAccount);
}

