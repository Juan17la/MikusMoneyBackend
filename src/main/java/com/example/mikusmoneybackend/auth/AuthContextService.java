package com.example.mikusmoneybackend.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.account.Account;
import com.example.mikusmoneybackend.account.AccountRepository;
import com.example.mikusmoneybackend.config.exception.AuthenticationException;
import com.example.mikusmoneybackend.config.exception.ResourceNotFoundException;
import com.example.mikusmoneybackend.credentials.Credential;
import com.example.mikusmoneybackend.credentials.CredentialRepository;
import com.example.mikusmoneybackend.miku.Miku;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing authentication context.
 * Provides methods to access the authenticated user, validate credentials,
 * and retrieve associated resources without passing user information through DTOs.
 * 
 * Usage:
 * - Inject this service into any component that needs access to the authenticated user
 * - Use getAuthenticatedMiku() to get the current user
 * - Use validatePin() or validatePassword() for sensitive operations
 * - Use validateAuth() for complete validation with optional PIN
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthContextService {

    private final AccountRepository accountRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== User Retrieval ====================

    /**
     * Retrieves the authenticated Miku from the security context.
     * 
     * @return The authenticated Miku entity
     * @throws AuthenticationException if user is not authenticated
     */
    public Miku getAuthenticatedMiku() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw AuthenticationException.notAuthenticated();
        }

        Object principal = authentication.getPrincipal();
        
        if (principal instanceof Miku miku) {
            return miku;
        }

        throw AuthenticationException.notAuthenticated();
    }

    /**
     * Checks if the current request is authenticated.
     * 
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof Miku;
    }

    /**
     * Gets the authenticated user's ID, or null if not authenticated.
     * 
     * @return User ID or null
     */
    public Long getAuthenticatedUserId() {
        if (!isAuthenticated()) {
            return null;
        }
        return getAuthenticatedMiku().getId();
    }

    // ==================== Account Operations ====================

    /**
     * Retrieves the account associated with the authenticated user.
     * 
     * @return The Account entity
     * @throws AuthenticationException if user is not authenticated
     * @throws ResourceNotFoundException if account is not found
     */
    public Account getAuthenticatedAccount() {
        Miku miku = getAuthenticatedMiku();
        return getAccountByMiku(miku);
    }

    /**
     * Retrieves the account associated with the given Miku.
     * 
     * @param miku The Miku entity
     * @return The Account entity
     * @throws ResourceNotFoundException if account is not found
     */
    public Account getAccountByMiku(Miku miku) {
        return accountRepository.findByMikuId(miku.getId())
                .orElseThrow(ResourceNotFoundException::account);
    }

    // ==================== Credential Operations ====================

    /**
     * Retrieves the credentials for the authenticated user.
     * 
     * @return The Credential entity
     * @throws AuthenticationException if user is not authenticated
     * @throws ResourceNotFoundException if credentials are not found
     */
    public Credential getAuthenticatedCredential() {
        Miku miku = getAuthenticatedMiku();
        return getCredentialByMiku(miku);
    }

    /**
     * Retrieves the credentials for the given Miku.
     * 
     * @param miku The Miku entity
     * @return The Credential entity
     * @throws ResourceNotFoundException if credentials are not found
     */
    public Credential getCredentialByMiku(Miku miku) {
        return credentialRepository.findByMikuId(miku.getId())
                .orElseThrow(ResourceNotFoundException::credentials);
    }

    // ==================== PIN Validation ====================

    /**
     * Validates the PIN code for the authenticated user.
     * 
     * @param pinCode The PIN code to validate
     * @throws AuthenticationException if PIN is invalid
     * @throws ResourceNotFoundException if credentials are not found
     */
    public void validatePin(String pinCode) {
        Credential credential = getAuthenticatedCredential();
        validatePinInternal(credential, pinCode);
    }

    /**
     * Validates the PIN code for the given Miku.
     * 
     * @param miku The Miku entity
     * @param pinCode The PIN code to validate
     * @throws AuthenticationException if PIN is invalid
     * @throws ResourceNotFoundException if credentials are not found
     */
    public void validatePin(Miku miku, String pinCode) {
        Credential credential = getCredentialByMiku(miku);
        validatePinInternal(credential, pinCode);
    }

    /**
     * Checks if the PIN is valid for the authenticated user without throwing an exception.
     * 
     * @param pinCode The PIN code to check
     * @return true if PIN is valid, false otherwise
     */
    public boolean isPinValid(String pinCode) {
        try {
            validatePin(pinCode);
            return true;
        } catch (AuthenticationException | ResourceNotFoundException e) {
            return false;
        }
    }

    // ==================== Password Validation ====================

    /**
     * Validates the password for the authenticated user.
     * 
     * @param password The password to validate
     * @throws AuthenticationException if password is invalid
     * @throws ResourceNotFoundException if credentials are not found
     */
    public void validatePassword(String password) {
        Credential credential = getAuthenticatedCredential();
        validatePasswordInternal(credential, password);
    }

    /**
     * Validates the password for the given Miku.
     * 
     * @param miku The Miku entity
     * @param password The password to validate
     * @throws AuthenticationException if password is invalid
     * @throws ResourceNotFoundException if credentials are not found
     */
    public void validatePassword(Miku miku, String password) {
        Credential credential = getCredentialByMiku(miku);
        validatePasswordInternal(credential, password);
    }

    /**
     * Checks if the password is valid for the authenticated user without throwing an exception.
     * 
     * @param password The password to check
     * @return true if password is valid, false otherwise
     */
    public boolean isPasswordValid(String password) {
        try {
            validatePassword(password);
            return true;
        } catch (AuthenticationException | ResourceNotFoundException e) {
            return false;
        }
    }

    // ==================== Complete Validation ====================

    /**
     * Performs complete authentication validation with optional PIN.
     * Returns a context object containing the Miku and Account.
     * 
     * @param pinCode The PIN code to validate (optional, can be null)
     * @return AuthContext with Miku and Account
     * @throws AuthenticationException if not authenticated or PIN is invalid
     * @throws ResourceNotFoundException if account is not found
     */
    public AuthContext validateAuth(String pinCode) {
        Miku miku = getAuthenticatedMiku();
        Account account = getAccountByMiku(miku);

        if (pinCode != null && !pinCode.isBlank()) {
            validatePin(miku, pinCode);
        }

        return new AuthContext(miku, account);
    }

    /**
     * Performs complete authentication validation requiring PIN.
     * 
     * @param pinCode The PIN code to validate (required)
     * @return AuthContext with Miku and Account
     * @throws AuthenticationException if not authenticated or PIN is invalid
     * @throws ResourceNotFoundException if account is not found
     * @throws IllegalArgumentException if PIN is null or blank
     */
    public AuthContext validateAuthWithPin(String pinCode) {
        if (pinCode == null || pinCode.isBlank()) {
            throw new IllegalArgumentException("PIN code is required");
        }
        return validateAuth(pinCode);
    }

    /**
     * Performs complete authentication validation requiring password.
     * 
     * @param password The password to validate (required)
     * @return AuthContext with Miku and Account
     * @throws AuthenticationException if not authenticated or password is invalid
     * @throws ResourceNotFoundException if account is not found
     */
    public AuthContext validateAuthWithPassword(String password) {
        Miku miku = getAuthenticatedMiku();
        Account account = getAccountByMiku(miku);
        validatePassword(miku, password);
        return new AuthContext(miku, account);
    }

    // ==================== Private Helpers ====================

    private void validatePinInternal(Credential credential, String pinCode) {
        if (pinCode == null || pinCode.isBlank()) {
            throw new IllegalArgumentException("PIN code cannot be null or empty");
        }

        if (!passwordEncoder.matches(pinCode, credential.getPinCode())) {
            log.debug("Invalid PIN attempt for user ID: {}", credential.getMiku().getId());
            throw AuthenticationException.invalidPin();
        }
    }

    private void validatePasswordInternal(Credential credential, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (!passwordEncoder.matches(password, credential.getPassword())) {
            log.debug("Invalid password attempt for user ID: {}", credential.getMiku().getId());
            throw AuthenticationException.invalidPassword();
        }
    }

    // ==================== Context Record ====================

    /**
     * Record containing authenticated user context.
     * Provides easy access to both Miku and Account entities.
     */
    public record AuthContext(Miku miku, Account account) {
        
        public Long userId() {
            return miku.getId();
        }

        public Long accountId() {
            return account.getId();
        }

        public String publicCode() {
            return miku.getPublicCode();
        }
    }
}
