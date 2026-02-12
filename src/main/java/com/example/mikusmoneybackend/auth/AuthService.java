package com.example.mikusmoneybackend.auth;

import java.math.BigDecimal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mikusmoneybackend.account.Account;
import com.example.mikusmoneybackend.account.AccountRepository;
import com.example.mikusmoneybackend.config.exception.AuthenticationException;
import com.example.mikusmoneybackend.config.exception.BusinessException;
import com.example.mikusmoneybackend.config.exception.ResourceNotFoundException;
import com.example.mikusmoneybackend.credentials.Credential;
import com.example.mikusmoneybackend.credentials.CredentialRepository;
import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.miku.MikuCreateRequest;
import com.example.mikusmoneybackend.miku.MikuMapper;
import com.example.mikusmoneybackend.miku.MikuRepository;
import com.example.mikusmoneybackend.miku.MikuResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service handling authentication operations: login, register, refresh, logout.
 * Uses cookie-based JWT authentication for stateless session management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MikuMapper mikuMapper;
    private final MikuRepository mikuRepository;
    private final CredentialRepository credentialRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final AuthContextService authContextService;

    // ==================== Register ====================

    /**
     * Registers a new user account.
     * Creates Miku, Credential, and Account entities.
     * Sets authentication cookies upon successful registration.
     */
    @Transactional
    public AuthResponse register(MikuCreateRequest request, HttpServletResponse response) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Validate unique constraints
        validateUniqueConstraints(request);

        // Validate confirmations
        validateConfirmations(request);
        // Validate lengths and formats for password and PIN
        validateLengthCredentials(request);

        // Create Miku entity
        Miku miku = mikuMapper.toEntity(request);

        if (!miku.isAdult()) {
            throw BusinessException.userNotAdult();
        }

        // Persist Miku
        Miku savedMiku = mikuRepository.save(miku);
        log.debug("Miku created with ID: {}", savedMiku.getId());


        Credential credential = Credential.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .pinCode(passwordEncoder.encode(request.getPinCode()))
                .miku(savedMiku)
                .build();

        credentialRepository.save(credential);

        // Create and persist Account
        Account account = Account.builder()
                .totalMoney(BigDecimal.ZERO)
                .miku(savedMiku)
                .build();

        accountRepository.save(account);

        // Generate tokens and set cookies
        setAuthenticationCookies(savedMiku, response);

        log.info("Registration successful for user ID: {}", savedMiku.getId());

        return AuthResponse.builder()
                .message("Registration successful")
                .build();
    }

    // ==================== Login ====================

    /**
     * Authenticates a user with email and PIN code.
     * Sets authentication cookies upon successful login.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        log.info("Processing login for email: {}", request.getEmail());

        // Find credential by email
        Credential credential = credentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.debug("Login failed: email not found - {}", request.getEmail());
                    return AuthenticationException.invalidCredentials();
                });

        // Validate PIN code
        if (!passwordEncoder.matches(request.getPinCode(), credential.getPinCode())) {
            log.debug("Login failed: invalid PIN for email - {}", request.getEmail());
            throw AuthenticationException.invalidCredentials();
        }

        Miku miku = credential.getMiku();

        // Generate tokens and set cookies
        setAuthenticationCookies(miku, response);

        log.info("Login successful for user ID: {}", miku.getId());

        return AuthResponse.builder()
                .message("Login successful")
                .build();
    }

    // ==================== Refresh Token ====================

    /**
     * Refreshes the access token using the refresh token from cookies.
     * Generates new access and refresh tokens.
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookies(request);

        if (refreshToken == null) {
            throw AuthenticationException.invalidToken();
        }

        try {
            // Validate refresh token
            if (jwtService.isTokenExpired(refreshToken)) {
                cookieService.clearAllAuthCookies(response);
                throw AuthenticationException.invalidToken();
            }

            Long userId = jwtService.getUserIdFromToken(refreshToken);

            // Load user
            Miku miku = mikuRepository.findById(userId)
                    .orElseThrow(ResourceNotFoundException::user);

            // Validate token belongs to user
            if (!jwtService.isTokenValid(refreshToken, userId)) {
                cookieService.clearAllAuthCookies(response);
                throw AuthenticationException.invalidToken();
            }

            // Generate new tokens
            setAuthenticationCookies(miku, response);

            log.info("Token refreshed for user ID: {}", userId);

            return AuthResponse.builder()
                    .message("Token refreshed successfully")
                    .build();

        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            cookieService.clearAllAuthCookies(response);
            throw AuthenticationException.invalidToken();
        }
    }

    // ==================== Logout ====================

    /**
     * Logs out the current user by clearing all authentication cookies.
     */
    public AuthResponse logout(HttpServletResponse response) {
        cookieService.clearAllAuthCookies(response);

        log.info("User logged out successfully");

        return AuthResponse.builder()
                .message("Logout successful")
                .build();
    }

    // ==================== Get Current User ====================

    /**
     * Returns the currently authenticated user's information.
     */
    public MikuResponse getCurrentUser() {
        Miku miku = authContextService.getAuthenticatedMiku();
        return mikuMapper.toResponse(miku);
    }

    // ==================== Private Helpers ====================

    private void validateUniqueConstraints(MikuCreateRequest request) {
        if (credentialRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.emailAlreadyExists();
        }

        if (credentialRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw BusinessException.phoneAlreadyExists();
        }
    }

    private void validateConfirmations(MikuCreateRequest request) {
        if (!request.getPassword().equals(request.getPasswordConfirmation())) {
            throw BusinessException.passwordMismatch();
        }

        if (!request.getPinCode().equals(request.getPinCodeConfirmation())) {
            throw BusinessException.pinMismatch();
        }
    }

    private void validateLengthCredentials(MikuCreateRequest request){
        String password = request.getPassword();
        if (password == null || password.length() < 8) {
            throw BusinessException.passwordLength();
        }

        String pin = request.getPinCode();
        if (pin == null || !pin.matches("^\\d{4,6}$")) {
            throw BusinessException.pinLength();
        }
    }

    private void setAuthenticationCookies(Miku miku, HttpServletResponse response) {
        String accessToken = jwtService.generateAccessToken(miku);
        String refreshToken = jwtService.generateRefreshToken(miku);

        cookieService.setAuthCookie(response, accessToken);
        cookieService.setRefreshCookie(response, refreshToken);
    }
}
