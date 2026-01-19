package com.example.mikusmoneybackend.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePinCodeRequest {

    @NotNull(message = "Miku ID is required")
    @Positive(message = "Miku ID must be positive")
    private Long mikuId;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "New PIN code is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN code must be 4-6 digits")
    private String newPinCode;
}
