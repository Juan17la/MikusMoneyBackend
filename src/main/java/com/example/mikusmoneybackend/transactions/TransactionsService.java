package com.example.mikusmoneybackend.transactions;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.account.Account;
import com.example.mikusmoneybackend.account.AccountRepository;
import com.example.mikusmoneybackend.auth.AuthContextService;
import com.example.mikusmoneybackend.auth.AuthContextService.AuthContext;
import com.example.mikusmoneybackend.config.exception.BusinessException;
import com.example.mikusmoneybackend.config.exception.ResourceNotFoundException;
import com.example.mikusmoneybackend.deposit.Deposit;
import com.example.mikusmoneybackend.deposit.DepositRepository;
import com.example.mikusmoneybackend.deposit.DepositRequest;
import com.example.mikusmoneybackend.deposit.DepositResponse;
import com.example.mikusmoneybackend.miku.Miku;
import com.example.mikusmoneybackend.transfer.Transfer;
import com.example.mikusmoneybackend.transfer.TransferMoneyRequest;
import com.example.mikusmoneybackend.transfer.TransferRepository;
import com.example.mikusmoneybackend.transfer.TransferResponse;
import com.example.mikusmoneybackend.transfer.TransactionHistoryResponse;
import com.example.mikusmoneybackend.withdraw.Withdraw;
import com.example.mikusmoneybackend.withdraw.WithdrawRepository;
import com.example.mikusmoneybackend.withdraw.WithdrawRequest;
import com.example.mikusmoneybackend.withdraw.WithdrawResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service for managing financial transactions: deposits, withdrawals, and transfers.
 * All transaction operations are protected by:
 * - Authentication (JWT token)
 * - PIN validation
 * - Idempotency key verification
 */
@Service
@RequiredArgsConstructor
public class TransactionsService {

    private final TransactionRepository transactionRepository;
    private final DepositRepository depositRepository;
    private final WithdrawRepository withdrawRepository;
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    
    private final IdempotencyService idempotencyService;
    private final AuthContextService authContextService;

    private static final int PAGE_SIZE = 10;

    // ==================== Deposit ====================

    /**
     * Performs a deposit operation.
     * 
     * @param request The deposit request containing amount and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return DepositResponse with transaction details
     */
    @Transactional
    public DepositResponse deposit(DepositRequest request, String idempotencyKey) {
        // 1. Validate idempotency key first (prevents duplicate transactions)
        idempotencyService.validate(idempotencyKey);
        
        // 2. Validate authentication and PIN
        AuthContext context = authContextService.validateAuthWithPin(request.getPinCode());

        BigDecimal amount = request.getAmount();

        validateMaxAmount(amount);

        // 3. Update account balance (validates amount internally)
        context.account().deposit(amount);
        accountRepository.save(context.account());

        // 4. Create and persist transaction record
        Deposit deposit = Deposit.builder()
                .amount(amount)
                .miku(context.miku())
                .idempotencyKey(idempotencyKey)
                .build();
        
        Deposit savedDeposit = depositRepository.save(deposit);
        
        return DepositResponse.builder()
                .id(savedDeposit.getId())
                .amount(savedDeposit.getAmount())
                .build();
    }

    // ==================== Withdraw ====================

    /**
     * Performs a withdrawal operation.
     * 
     * @param request The withdraw request containing amount and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return WithdrawResponse with transaction details
     */
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request, String idempotencyKey) {
        // 1. Validate idempotency key first
        idempotencyService.validate(idempotencyKey);
        
        // 2. Validate authentication and PIN
        AuthContext context = authContextService.validateAuthWithPin(request.getPinCode());
        
        BigDecimal amount = request.getAmount();

        // 3. Update account balance (validates sufficient funds internally)
        context.account().withdraw(amount);
        accountRepository.save(context.account());

        // 4. Create and persist transaction record
        Withdraw withdraw = Withdraw.builder()
                .amount(amount)
                .miku(context.miku())
                .idempotencyKey(idempotencyKey)
                .build();
        
        Withdraw savedWithdraw = withdrawRepository.save(withdraw);

        return WithdrawResponse.builder()
                .id(savedWithdraw.getId())
                .amount(savedWithdraw.getAmount())
                .build();
    }

    // ==================== Transfer ====================

    /**
     * Performs a money transfer to another user.
     * 
     * @param request The transfer request containing receiver code, amount, and PIN
     * @param idempotencyKey Unique key to prevent duplicate transactions
     * @return TransferResponse with transaction details
     */
    @Transactional
    public TransferResponse transfer(TransferMoneyRequest request, String idempotencyKey) {
        // 1. Validate idempotency key first
        idempotencyService.validate(idempotencyKey);
        
        // 2. Validate authentication and PIN
        AuthContext context = authContextService.validateAuthWithPin(request.getPinCode());

        // 3. Find and validate receiver account
        Account receiverAccount = accountRepository.findByMiku_PublicCode(request.getReceiverPublicCode())
                .orElseThrow(() -> ResourceNotFoundException.receiverAccount());
        
        // 4. Validate not sending to self
        if (receiverAccount.getMiku().getId().equals(context.miku().getId())) {
            throw new IllegalArgumentException("Cannot transfer money to yourself");
        }
        
        // 5. Execute transfer (validates sufficient funds internally)
        BigDecimal amount = request.getAmount();
        validateMaxAmount(amount);
        context.account().transfer(receiverAccount, amount);
        accountRepository.save(context.account());
        accountRepository.save(receiverAccount);

        // 6. Create and persist transaction record
        Transfer transfer = Transfer.builder()
                .amount(amount)
                .sender(context.miku())
                .receiver(receiverAccount.getMiku())
                .idempotencyKey(idempotencyKey)
                .build();
        
        Transfer savedTransfer = transferRepository.save(transfer);

        return TransferResponse.builder()
                .id(savedTransfer.getId())
                .amount(savedTransfer.getAmount())
                .build();
    }

    // ==================== Transaction History ====================

    /**
     * Retrieves the transaction history for the authenticated user.
     * Returns transactions in descending order by creation date.
     * 
     * @param page The page number (0-based)
     * @return Page of transaction history responses
     */
    @Transactional
    public Page<TransactionHistoryResponse> getTransactionHistory(int page) {
        Miku miku = authContextService.getAuthenticatedMiku();
        
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<Transaction> transactions = transactionRepository.findAllByMikuId(miku.getId(), pageable);
        
        return transactions.map(this::mapToHistoryResponse);
    }

    /**
     * Maps a Transaction entity to a TransactionHistoryResponse.
     * Handles polymorphic transaction types.
     */
    private TransactionHistoryResponse mapToHistoryResponse(Transaction transaction) {
        TransactionHistoryResponse.TransactionHistoryResponseBuilder builder = TransactionHistoryResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .createdAt(transaction.getCreatedAt());

        if (transaction instanceof Deposit deposit) {
            builder.transactionType("DEPOSIT")
                   .owner(deposit.getMiku().getFullName());
        } else if (transaction instanceof Withdraw withdraw) {
            builder.transactionType("WITHDRAW")
                   .owner(withdraw.getMiku().getFullName());
        } else if (transaction instanceof Transfer transfer) {
            builder.transactionType("TRANSFER")
                   .from(transfer.getSender().getFullName())
                   .to(transfer.getReceiver().getFullName());
        }

        return builder.build();
    }

    public void validateMaxAmount(BigDecimal amount){
        BigDecimal limit = new BigDecimal("10000.00");

        if(amount == null || amount.compareTo(limit) > 0 ) {
            throw BusinessException.overMaxAmout();
        }
    }
}
