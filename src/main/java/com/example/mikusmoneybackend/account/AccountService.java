package com.example.mikusmoneybackend.account;

import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.auth.AuthContextService;
import com.example.mikusmoneybackend.auth.AuthContextService.AuthContext;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service for account-related operations.
 * Provides account information retrieval for authenticated users.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AuthContextService authContextService;

    /**
     * Retrieves the account details for the authenticated user.
     * No PIN required for read-only operation.
     * 
     * @return AccountResponse with account balance and user information
     */
    @Transactional
    public AccountResponse getAccountDetail() {
        // Validate authentication (no PIN required for read-only operation)
        AuthContext context = authContextService.validateAuth(null);
        
        Account account = context.account();
        
        return AccountResponse.builder()
                .id(account.getId())
                .totalMoney(account.getTotalMoney())
                .fullName(context.miku().getFullName())
                .publicCode(context.miku().getPublicCode())
                .build();
    }

    /**
     * Checks if the authenticated user's account is empty.
     * 
     * @return true if account balance is zero
     */
    public boolean isAccountEmpty() {
        Account account = authContextService.getAuthenticatedAccount();
        return account.isEmpty();
    }
}
