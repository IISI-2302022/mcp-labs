package com.ming.mymcpserver.tool;

import com.ming.mymcpserver.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountTool {

    private final AccountService accountService;

    @McpTool(description = "查詢帳戶餘額，輸入帳號回傳戶名與目前餘額")
    public Mono<String> getBalance(@McpToolParam(description = "帳號，例如 ACC001") String accountNo) {
        log.info("查詢餘額: accountNo={}", accountNo);
        return Mono.fromCallable(() -> accountService.getBalance(accountNo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @McpTool(description = "執行轉帳，從來源帳號轉指定金額到目標帳號，回傳轉帳結果")
    public Mono<String> transfer(
            @McpToolParam(description = "轉出帳號") String fromAccountNo,
            @McpToolParam(description = "轉入帳號") String toAccountNo,
            @McpToolParam(description = "轉帳金額") BigDecimal amount) {
        log.info("執行轉帳: {} -> {}, 金額: {}", fromAccountNo, toAccountNo, amount);
        return Mono.fromCallable(() -> accountService.transfer(fromAccountNo, toAccountNo, amount))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @McpTool(description = "查詢轉帳紀錄，輸入帳號查詢該帳號所有轉入與轉出紀錄")
    public Mono<String> getTransferHistory(@McpToolParam(description = "要查詢的帳號") String accountNo) {
        log.info("查詢轉帳紀錄: accountNo={}", accountNo);
        return Mono.fromCallable(() -> accountService.getTransferHistory(accountNo))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
