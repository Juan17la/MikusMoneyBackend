package com.example.mikusmoneybackend.account;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private Long id;
    private BigDecimal totalMoney;
    private String fullName;
    private String publicCode;
}
