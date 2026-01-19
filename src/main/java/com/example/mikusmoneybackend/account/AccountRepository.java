package com.example.mikusmoneybackend.account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Account entity operations.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by Miku ID.
     */
    Optional<Account> findByMikuId(Long mikuId);

    /**
     * Checks if an account exists for the given Miku ID.
     */
    boolean existsByMikuId(Long mikuId);

    /**
     * Finds an account by the associated Miku's public code.
     * Used for transfers to locate the receiver's account.
     */
    Optional<Account> findByMiku_PublicCode(String publicCode);
}
