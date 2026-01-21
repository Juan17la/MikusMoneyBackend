package com.example.mikusmoneybackend.savings;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for savings pig operations.
 * Handles creation, deposits, breaking, and retrieval of savings pigs.
 */
@RestController
@RequestMapping("/api/savings-pigs")
@RequiredArgsConstructor
public class SavingsPigController {

    private final SavingsPigService savingsPigService;

    /**
     * Creates a new savings pig for the authenticated user.
     * 
     * @param request The creation request with goal amount and name
     * @return The created savings pig details
     */
    @PostMapping
    public ResponseEntity<SavingsPigResponse> createSavingsPig(
            @Valid @RequestBody SavingsPigCreationRequest request) {
        return ResponseEntity.ok(savingsPigService.createSavingsPig(request));
    }

    /**
     * Retrieves all savings pigs for the authenticated user.
     * 
     * @return List of all savings pigs
     */
    @GetMapping
    public ResponseEntity<List<SavingsPigResponse>> getSavingsPigs() {
        return ResponseEntity.ok(savingsPigService.getSavingsPigs());
    }

    /**
     * Retrieves only active (not broken) savings pigs.
     * 
     * @return List of active savings pigs
     */
    @GetMapping("/active")
    public ResponseEntity<List<SavingsPigResponse>> getActiveSavingsPigs() {
        return ResponseEntity.ok(savingsPigService.getActiveSavingsPigs());
    }

    /**
     * Deposits money from the account into a savings pig.
     * Requires PIN validation.
     * 
     * @param pigId The ID of the pig to deposit into
     * @param request The deposit request containing amount and PIN
     * @return The updated savings pig details
     */
    @PostMapping("/{pigId}/deposit")
    public ResponseEntity<SavingsPigResponse> depositToSavingsPig(
            @PathVariable Long pigId,
            @Valid @RequestBody SavingsPigDepositRequest request) {
        return ResponseEntity.ok(savingsPigService.depositToSavingsPig(pigId, request));
    }

    /**
     * Breaks a savings pig and transfers the saved money back to the account.
     * Requires PIN validation.
     * 
     * @param pigId The ID of the pig to break
     * @param request The break request containing PIN
     * @return The broken savings pig details
     */
    @PostMapping("/{pigId}/break")
    public ResponseEntity<SavingsPigResponse> breakSavingsPig(
            @PathVariable Long pigId,
            @Valid @RequestBody SavingsPigBreakRequest request) {
        return ResponseEntity.ok(savingsPigService.breakSavingsPig(pigId, request));
    }
}
