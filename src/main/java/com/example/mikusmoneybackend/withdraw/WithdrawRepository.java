package com.example.mikusmoneybackend.withdraw;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Withdraw entity operations.
 */
@Repository
public interface WithdrawRepository extends JpaRepository<Withdraw, Long> {
}
