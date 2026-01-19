package com.example.mikusmoneybackend.transfer;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionHistoryResponse {

    private Long id;
    private String transactionType;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    
    // For deposits and withdrawals
    private String owner;
    
    // For send transactions
    private String from;
    private String to;
}
