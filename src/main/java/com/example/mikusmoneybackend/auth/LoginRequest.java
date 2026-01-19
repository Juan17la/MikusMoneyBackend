package com.example.mikusmoneybackend.auth;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "PIN code is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN code must be 4-6 digits")
    private String pinCode;
}
