package com.ming.mymcpserver.repository;

import com.ming.mymcpserver.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByAccountNo(String accountNo);
}

