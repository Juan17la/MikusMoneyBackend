package com.example.mikusmoneybackend.account;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for account operations.
 * Handles account information retrieval.
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Retrieves the account details for the authenticated user.
     * 
     * @return Account details including balance and user information
     */
    @GetMapping
    public ResponseEntity<AccountResponse> getAccountDetail() {
        return ResponseEntity.ok(accountService.getAccountDetail());
    }

    /**
     * Checks if the authenticated user's account is empty.
     * 
     * @return true if account balance is zero
     */
    @GetMapping("/empty")
    public ResponseEntity<Boolean> isAccountEmpty() {
        return ResponseEntity.ok(accountService.isAccountEmpty());
    }
}
