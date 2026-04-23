package com.ming.mymcpserver.service;

import com.ming.mymcpserver.model.TransferRecord;
import com.ming.mymcpserver.repository.AccountRepository;
import com.ming.mymcpserver.repository.TransferRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransferRecordRepository transferRecordRepository;

    public String getBalance(String accountNo, String inputOwerName) {
        val account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("查無此帳號: " + accountNo));

        val ownerName = account.getOwnerName();

        if (!ownerName.equals(inputOwerName)) {
            return String.format("查詢失敗：查詢操作者 與 帳戶擁有者 不一致，操作者 %s，擁有者 %s", inputOwerName, ownerName);
        }

        return String.format("帳號: %s, 戶名: %s, 餘額: %s",
                account.getAccountNo(), account.getOwnerName(), account.getBalance());
    }


    public String transfer(String fromAccountNo, String toAccountNo, BigDecimal amount, String inputOwerName) {

        if (fromAccountNo.equals(toAccountNo)) {
            return "轉帳失敗：轉出與轉入帳號不可相同";
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "轉帳失敗：金額必須大於 0";
        }

        val from = accountRepository.findByAccountNo(fromAccountNo)
                .orElseThrow(() -> new RuntimeException("查無轉出帳號: " + fromAccountNo));

        if (from.getBalance().compareTo(amount) < 0) {
            return String.format("轉帳失敗：餘額不足，目前餘額 %s，欲轉出 %s", from.getBalance(), amount);
        }

        val ownerName = from.getOwnerName();
        if (!ownerName.equals(inputOwerName)) {
            return String.format("轉帳失敗：轉帳操作者 與 轉出帳戶擁有者 不一致，操作者 %s，擁有者 %s", inputOwerName, ownerName);
        }

        val to = accountRepository.findByAccountNo(toAccountNo)
                .orElseThrow(() -> new RuntimeException("查無轉入帳號: " + toAccountNo));

        val now = LocalDateTime.now();

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
        accountRepository.save(from);
        accountRepository.save(to);

        val record = new TransferRecord();
        record.setFromAccount(fromAccountNo);
        record.setToAccount(toAccountNo);
        record.setAmount(amount);
        record.setTransferTime(now);
        transferRecordRepository.save(record);

        return String.format("轉帳成功！%s(%s) -> %s(%s)，金額: %s，轉出後餘額: %s",
                fromAccountNo, from.getOwnerName(),
                toAccountNo, to.getOwnerName(),
                amount, from.getBalance());
    }

    public String getTransferHistory(String accountNo) {
        val records = transferRecordRepository
                .findByFromAccountOrToAccountOrderByTransferTimeDesc(accountNo, accountNo);

        if (records.isEmpty()) {
            return "帳號 " + accountNo + " 目前無任何轉帳紀錄";
        }

        return records.stream()
                .map(r -> String.format("[%s] %s -> %s, 金額: %s",
                        r.getTransferTime(), r.getFromAccount(), r.getToAccount(), r.getAmount()))
                .collect(Collectors.joining("\n"));
    }
}
