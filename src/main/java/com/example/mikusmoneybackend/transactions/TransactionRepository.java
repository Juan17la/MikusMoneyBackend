package com.example.mikusmoneybackend.transactions;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity operations.
 * Handles all transaction types (Deposit, Withdraw, Transfer) through inheritance.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Checks if a transaction with the given idempotency key already exists.
     * Used to prevent duplicate transactions.
     * 
     * @param idempotencyKey The unique idempotency key
     * @return true if a transaction with this key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Finds all transactions where the user is involved (as owner, sender, or receiver).
     * This query handles the polymorphic nature of transactions.
     * 
     * @param mikuId The user ID
     * @param pageable Pagination information
     * @return Page of transactions
     */
    @Query("""
        SELECT t FROM Transaction t 
        WHERE (TYPE(t) = com.example.mikusmoneybackend.deposit.Deposit AND 
               TREAT(t AS com.example.mikusmoneybackend.deposit.Deposit).miku.id = :mikuId)
           OR (TYPE(t) = com.example.mikusmoneybackend.withdraw.Withdraw AND 
               TREAT(t AS com.example.mikusmoneybackend.withdraw.Withdraw).miku.id = :mikuId)
           OR (TYPE(t) = com.example.mikusmoneybackend.transfer.Transfer AND 
               (TREAT(t AS com.example.mikusmoneybackend.transfer.Transfer).sender.id = :mikuId OR 
                TREAT(t AS com.example.mikusmoneybackend.transfer.Transfer).receiver.id = :mikuId))
        ORDER BY t.createdAt DESC
        """)
    Page<Transaction> findAllByMikuId(@Param("mikuId") Long mikuId, Pageable pageable);
}
