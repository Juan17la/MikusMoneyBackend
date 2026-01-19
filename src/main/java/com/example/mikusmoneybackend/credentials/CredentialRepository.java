package com.example.mikusmoneybackend.credentials;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Credential entity operations.
 */
@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    /**
     * Finds credentials by email.
     */
    Optional<Credential> findByEmail(String email);

    /**
     * Finds credentials by phone number.
     */
    Optional<Credential> findByPhoneNumber(String phoneNumber);

    /**
     * Finds credentials by Miku ID.
     */
    Optional<Credential> findByMikuId(Long mikuId);

    /**
     * Checks if credentials exist for the given email.
     */
    boolean existsByEmail(String email);

    /**
     * Checks if credentials exist for the given phone number.
     */
    boolean existsByPhoneNumber(String phoneNumber);
}
