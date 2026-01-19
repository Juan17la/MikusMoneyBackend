package com.example.mikusmoneybackend.deposit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Deposit entity operations.
 */
@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {
}
