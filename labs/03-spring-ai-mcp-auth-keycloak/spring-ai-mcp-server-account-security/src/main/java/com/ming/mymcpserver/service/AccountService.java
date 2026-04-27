package com.ming.mymcpserver.service;

import com.ming.mymcpserver.model.TransferRecord;
import com.ming.mymcpserver.repository.AccountRepository;
import com.ming.mymcpserver.repository.TransferRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    public String getBalance(String accountNo, Authentication authentication) {

        val account = accountRepository.findByAccountNo(accountNo);
        if (account == null) {
            return String.format("查無此帳號: %s", accountNo);
        }

        val ownerName = account.getOwnerName();
        val inputOwerName = authentication.getName();
        if (!isAdmin(authentication) && !ownerName.equals(inputOwerName)) {
            return String.format("查詢失敗：查詢操作者與帳戶擁有者不一致，操作者 %s，擁有者 %s", inputOwerName, ownerName);
        }

        return String.format("帳號: %s, 戶名: %s, 餘額: %s",
                account.getAccountNo(), ownerName, account.getBalance()
        );
    }


    public String transfer(String fromAccountNo, String toAccountNo, BigDecimal amount, Authentication authentication) {

        if (fromAccountNo.equals(toAccountNo)) {
            return "轉帳失敗：轉出與轉入帳號不可相同";
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "轉帳失敗：金額必須大於 0";
        }

        val from = accountRepository.findByAccountNo(fromAccountNo);
        if (from == null) {
            return String.format("查無轉出帳號: %s", fromAccountNo);
        }

        val ownerName = from.getOwnerName();
        val inputOwerName = authentication.getName();
        if (!ownerName.equals(inputOwerName)) {
            return String.format("轉帳失敗：轉帳操作者 與 轉出帳戶擁有者 不一致，操作者 %s，擁有者 %s", inputOwerName, ownerName);
        }

        if (from.getBalance().compareTo(amount) < 0) {
            return String.format("轉帳失敗：餘額不足，目前餘額 %s，欲轉出 %s", from.getBalance(), amount);
        }

        val to = accountRepository.findByAccountNo(toAccountNo);
        if (to == null) {
            return String.format("查無轉入帳號: %s", toAccountNo);
        }

        val now = LocalDateTime.now();

        from.setBalance(from.getBalance().subtract(amount));
        // todo 先不考慮溢出問題 , 大家都是牛馬
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
                fromAccountNo, ownerName,
                toAccountNo, to.getOwnerName(),
                amount, from.getBalance()
        );
    }

    public String getTransferHistory(String accountNo, Authentication authentication) {

        val account = accountRepository.findByAccountNo(accountNo);
        if (account == null) {
            return String.format("查無此帳號: %s", accountNo);
        }

        val ownerName = account.getOwnerName();
        val inputOwerName = authentication.getName();
        if (!isAdmin(authentication) && !ownerName.equals(inputOwerName)) {
            return String.format("查詢失敗：查詢操作者與帳戶擁有者不一致，操作者 %s，擁有者 %s", inputOwerName, ownerName);
        }

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


    public static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_mcp-server_admin"::equalsIgnoreCase);
    }

}
