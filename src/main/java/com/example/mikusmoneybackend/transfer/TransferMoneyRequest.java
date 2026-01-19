package com.example.mikusmoneybackend.transfer;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferMoneyRequest {

    @NotBlank(message = "Receiver public code is required")
    private String receiverPublicCode;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotBlank(message = "PIN code is required for sending money")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN code must be 4-6 digits")
    private String pinCode;
}
