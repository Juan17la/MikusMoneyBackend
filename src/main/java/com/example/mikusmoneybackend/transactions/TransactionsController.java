package com.example.mikusmoneybackend.transactions;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mikusmoneybackend.deposit.DepositRequest;
import com.example.mikusmoneybackend.deposit.DepositResponse;
import com.example.mikusmoneybackend.transfer.TransferMoneyRequest;
import com.example.mikusmoneybackend.transfer.TransferResponse;
import com.example.mikusmoneybackend.transfer.TransactionHistoryResponse;
import com.example.mikusmoneybackend.withdraw.WithdrawRequest;
import com.example.mikusmoneybackend.withdraw.WithdrawResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for financial transaction operations.
 * Handles deposits, withdrawals, transfers, and transaction history.
 * All operations require authentication via JWT and PIN validation.
 * All mutating operations require an idempotency key to prevent duplicates.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionsService transactionsService;

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";

    /**
     * Performs a deposit operation.
     * 
     * @param request The deposit request containing amount and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return The deposit transaction details
     */
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse> deposit(
            @Valid @RequestBody DepositRequest request,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey) {
        return ResponseEntity.ok(transactionsService.deposit(request, idempotencyKey));
    }

    /**
     * Performs a withdrawal operation.
     * 
     * @param request The withdraw request containing amount and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return The withdrawal transaction details
     */
    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey) {
        return ResponseEntity.ok(transactionsService.withdraw(request, idempotencyKey));
    }

    /**
     * Performs a money transfer to another user.
     * 
     * @param request The transfer request containing receiver code, amount, and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return The transfer transaction details
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferMoneyRequest request,
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey) {
        return ResponseEntity.ok(transactionsService.transfer(request, idempotencyKey));
    }

    /**
     * Retrieves the transaction history for the authenticated user.
     * 
     * @param page The page number (0-based, default 0)
     * @return Page of transaction history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<TransactionHistoryResponse>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(transactionsService.getTransactionHistory(page));
    }
}
