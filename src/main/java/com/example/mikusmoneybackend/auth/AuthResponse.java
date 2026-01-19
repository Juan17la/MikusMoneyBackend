package com.example.mikusmoneybackend.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token; // Used internally, not sent in response body
    private String message; // Success/error message sent to client
}
