package com.example.mikusmoneybackend.withdraw;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawResponse {

    private Long id;
    private BigDecimal amount;
}
