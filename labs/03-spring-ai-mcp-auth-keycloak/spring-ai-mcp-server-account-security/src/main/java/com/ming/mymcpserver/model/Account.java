package com.ming.mymcpserver.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_no", length = 20, unique = true, nullable = false)
    private String accountNo;

    @Column(name = "owner_name", length = 50, nullable = false)
    private String ownerName;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;

    @Version
    private Long version;
}
