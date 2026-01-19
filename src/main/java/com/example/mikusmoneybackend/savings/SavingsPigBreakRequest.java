package com.example.mikusmoneybackend.savings;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsPigBreakRequest {

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN code must be 4-6 digits")
    private String pinCode;
}
