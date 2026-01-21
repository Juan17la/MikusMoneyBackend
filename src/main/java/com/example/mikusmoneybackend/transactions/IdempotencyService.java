package com.example.mikusmoneybackend.transactions;

import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.config.exception.DuplicateTransactionException;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for checking idempotency keys to prevent duplicate transactions.
 * Ensures that a transaction with the same idempotency key has not been processed before.
 * 
 * Idempotency keys should be generated client-side (typically UUIDs) and sent with each
 * transaction request. This prevents duplicate transactions if:
 * - Network issues cause request retries
 * - User accidentally clicks submit multiple times
 * - System errors cause the request to be replayed
 */
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final TransactionRepository transactionRepository;

    /**
     * Validates that the given idempotency key has not been used before.
     * 
     * @param idempotencyKey The unique key to validate (should be a UUID from client)
     * @throws DuplicateTransactionException if the idempotency key has already been used
     * @throws IllegalArgumentException if the idempotency key is null or blank
     */
    public void validate(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key is required");
        }

        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateTransactionException("Transaction already processed with this idempotency key");
        }
    }

    /**
     * Checks if an idempotency key has been used without throwing an exception.
     * 
     * @param idempotencyKey The key to check
     * @return true if the key has already been used, false otherwise
     */
    public boolean isKeyUsed(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return false;
        }
        return transactionRepository.existsByIdempotencyKey(idempotencyKey);
    }
}
