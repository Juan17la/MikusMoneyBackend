package com.example.mikusmoneybackend.savings;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mikusmoneybackend.account.AccountRepository;
import com.example.mikusmoneybackend.auth.AuthContextService;
import com.example.mikusmoneybackend.auth.AuthContextService.AuthContext;
import com.example.mikusmoneybackend.config.exception.ResourceNotFoundException;
import com.example.mikusmoneybackend.miku.Miku;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * Service for managing savings pigs (goals).
 * Allows users to create savings goals, deposit money, and break pigs to retrieve funds.
 */
@Service
@RequiredArgsConstructor
public class SavingsPigService {

    private final SavingsPigRepository savingsPigRepository;
    private final AccountRepository accountRepository;
    private final AuthContextService authContextService;

    private static final int MAX_ACTIVE_PIGS = 10;
    private static final BigDecimal MIN_GOAL_AMOUNT = new BigDecimal("1.00");

    // ==================== Create Savings Pig ====================

    /**
     * Creates a new savings pig for the authenticated user.
     * No PIN required for creation as no money movement occurs.
     * 
     * @param request The creation request with goal amount and name
     * @return SavingsPigResponse with the created pig details
     */
    @Transactional
    public SavingsPigResponse createSavingsPig(SavingsPigCreationRequest request) {
        // 1. Get authenticated user (no PIN required for creation)
        Miku miku = authContextService.getAuthenticatedMiku();

        // 2. Validate goal amount
        validateGoalAmount(request.getGoal());

        // 3. Validate user doesn't exceed max active pigs
        validateActivePigsLimit(miku.getId());

        // 4. Create and save savings pig
        SavingsPig savingsPig = SavingsPig.builder()
                .goal(request.getGoal())
                .goalName(request.getNameGoal())
                .savedMoney(BigDecimal.ZERO)
                .broken(false)
                .miku(miku)
                .build();
        
        SavingsPig savedPig = savingsPigRepository.save(savingsPig);
        
        return mapToResponse(savedPig);
    }

    // ==================== Break Savings Pig ====================

    /**
     * Breaks a savings pig and transfers the saved money back to the account.
     * Requires PIN validation as money is being moved.
     * 
     * @param pigId The ID of the pig to break
     * @param request The break request containing PIN
     * @return SavingsPigResponse with the broken pig details
     */
    @Transactional
    public SavingsPigResponse breakSavingsPig(Long pigId, SavingsPigBreakRequest request) {
        // 1. Validate authentication and PIN
        AuthContext context = authContextService.validateAuthWithPin(request.getPinCode());

        // 2. Find and validate pig ownership
        SavingsPig savingsPig = findPigByIdAndOwner(pigId, context.miku().getId());
        
        // 3. Validate pig is not already broken
        validatePigNotBroken(savingsPig);

        // 4. Break pig and get saved amount
        BigDecimal savedAmount = savingsPig.breakPig();

        // 5. Transfer savings back to account (if any)
        if (savedAmount.compareTo(BigDecimal.ZERO) > 0) {
            context.account().deposit(savedAmount);
            accountRepository.save(context.account());
        }
        
        // 6. Save the broken pig
        SavingsPig brokenPig = savingsPigRepository.save(savingsPig);

        return mapToResponse(brokenPig);
    }

    // ==================== Deposit to Savings Pig ====================

    /**
     * Deposits money from the user's account into a savings pig.
     * Requires PIN validation as money is being moved.
     * 
     * @param pigId The ID of the pig to deposit into
     * @param request The deposit request containing amount and PIN
     * @return SavingsPigResponse with updated pig details
     */
    @Transactional
    public SavingsPigResponse depositToSavingsPig(Long pigId, SavingsPigDepositRequest request) {
        // 1. Validate authentication and PIN
        AuthContext context = authContextService.validateAuthWithPin(request.getPinCode());

        // 2. Find and validate pig ownership
        SavingsPig savingsPig = findPigByIdAndOwner(pigId, context.miku().getId());

        // 3. Validate pig is not broken
        validatePigNotBroken(savingsPig);

        // 4. Validate sufficient account balance
        BigDecimal amount = request.getAmount();
        if (!context.account().hasEnoughBalance(amount)) {
            throw new IllegalStateException("Insufficient account balance");
        }

        // 5. Withdraw from account
        context.account().withdraw(amount);
        accountRepository.save(context.account());

        // 6. Add to savings pig
        savingsPig.addMoney(amount);
        SavingsPig savedPig = savingsPigRepository.save(savingsPig);

        return mapToResponse(savedPig);
    }

    // ==================== Get Savings Pigs ====================

    /**
     * Retrieves all savings pigs for the authenticated user.
     * No PIN required for read-only operation.
     * 
     * @return List of all user's savings pigs
     */
    public List<SavingsPigResponse> getSavingsPigs() {
        Miku miku = authContextService.getAuthenticatedMiku();
        
        List<SavingsPig> pigsList = savingsPigRepository.findByMikuId(miku.getId());

        return pigsList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retrieves only active (not broken) savings pigs.
     * 
     * @return List of active savings pigs
     */
    public List<SavingsPigResponse> getActiveSavingsPigs() {
        Miku miku = authContextService.getAuthenticatedMiku();
        
        List<SavingsPig> pigsList = savingsPigRepository.findActivePigsByMikuId(miku.getId());

        return pigsList.stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ==================== Private Helpers ====================

    private SavingsPig findPigByIdAndOwner(Long pigId, Long mikuId) {
        return savingsPigRepository.findByIdAndMikuId(pigId, mikuId)
                .orElseThrow(() -> ResourceNotFoundException.savingsPig());
    }

    private void validatePigNotBroken(SavingsPig savingsPig) {
        if (savingsPig.getBroken()) {
            throw new IllegalStateException("Savings pig has already been broken");
        }
    }

    private void validateGoalAmount(BigDecimal goal) {
        if (goal.compareTo(MIN_GOAL_AMOUNT) < 0) {
            throw new IllegalArgumentException("Goal amount must be at least " + MIN_GOAL_AMOUNT);
        }
    }

    private void validateActivePigsLimit(Long mikuId) {
        long activePigs = savingsPigRepository.countActivePigsByMikuId(mikuId);
        if (activePigs >= MAX_ACTIVE_PIGS) {
            throw new IllegalStateException("Maximum number of active savings pigs reached (" + MAX_ACTIVE_PIGS + ")");
        }
    }

    private SavingsPigResponse mapToResponse(SavingsPig savingsPig) {
        return SavingsPigResponse.builder()
                .id(savingsPig.getId())
                .savedMoney(savingsPig.getSavedMoney())
                .goal(savingsPig.getGoal())
                .goalName(savingsPig.getGoalName())
                .broken(savingsPig.getBroken())
                .brokenAt(savingsPig.getBrokenAt())
                .createdAt(savingsPig.getCreatedAt())
                .mikuId(savingsPig.getMiku().getId())
                .build();
    }
}
