package com.example.mikusmoneybackend.miku;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Miku entity operations.
 */
@Repository
public interface MikuRepository extends JpaRepository<Miku, Long> {

    /**
     * Finds a Miku by their public code.
     */
    Optional<Miku> findByPublicCode(String publicCode);

    /**
     * Checks if a Miku exists by public code.
     */
    boolean existsByPublicCode(String publicCode);
}
