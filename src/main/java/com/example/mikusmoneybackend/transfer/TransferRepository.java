package com.example.mikusmoneybackend.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transfer entity operations.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
