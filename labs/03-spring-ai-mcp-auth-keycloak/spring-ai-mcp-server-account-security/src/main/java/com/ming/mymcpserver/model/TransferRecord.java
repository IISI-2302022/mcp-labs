package com.ming.mymcpserver.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transfer_record")
public class TransferRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_account", length = 20, nullable = false)
    private String fromAccount;

    @Column(name = "to_account", length = 20, nullable = false)
    private String toAccount;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "transfer_time", nullable = false)
    private LocalDateTime transferTime;
}

