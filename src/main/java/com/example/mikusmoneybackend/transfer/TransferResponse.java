package com.example.mikusmoneybackend.transfer;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private Long id;
    private BigDecimal amount;
}
