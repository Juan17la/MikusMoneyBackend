package com.example.mikusmoneybackend.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mikusmoneybackend.miku.MikuCreateRequest;
import com.example.mikusmoneybackend.miku.MikuResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for authentication operations.
 * Handles registration, login, token refresh, and logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     * 
     * @param request The registration request containing user details
     * @param response HTTP response for setting cookies
     * @return Success message
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody MikuCreateRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.register(request, response));
    }

    /**
     * Authenticates a user with email and PIN code.
     * 
     * @param request The login request containing credentials
     * @param response HTTP response for setting cookies
     * @return Success message
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    /**
     * Refreshes the access token using the refresh token from cookies.
     * 
     * @param request HTTP request containing refresh token cookie
     * @param response HTTP response for setting new cookies
     * @return Success message
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }

    /**
     * Logs out the current user by clearing authentication cookies.
     * 
     * @param response HTTP response for clearing cookies
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(response));
    }

    /**
     * Returns the currently authenticated user's information.
     * 
     * @return Current user details
     */
    @GetMapping("/me")
    public ResponseEntity<MikuResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
