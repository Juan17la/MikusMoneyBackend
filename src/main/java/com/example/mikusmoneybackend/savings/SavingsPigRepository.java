package com.example.mikusmoneybackend.savings;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for SavingsPig entity operations.
 */
@Repository
public interface SavingsPigRepository extends JpaRepository<SavingsPig, Long> {

    /**
     * Finds all savings pigs for a given user that are not broken.
     * 
     * @param mikuId The user ID
     * @return List of active savings pigs
     */
    @Query("SELECT s FROM SavingsPig s WHERE s.miku.id = :mikuId AND s.broken = false")
    List<SavingsPig> findActivePigsByMikuId(@Param("mikuId") Long mikuId);

    /**
     * Finds all savings pigs for a given user (including broken ones).
     * 
     * @param mikuId The user ID
     * @return List of all savings pigs
     */
    List<SavingsPig> findByMikuId(Long mikuId);

    /**
     * Finds a savings pig by ID and verifies ownership.
     * 
     * @param id The savings pig ID
     * @param mikuId The user ID
     * @return Optional containing the savings pig if found and owned by the user
     */
    Optional<SavingsPig> findByIdAndMikuId(Long id, Long mikuId);

    /**
     * Counts the number of active (not broken) savings pigs for a user.
     * 
     * @param mikuId The user ID
     * @return Count of active savings pigs
     */
    @Query("SELECT COUNT(s) FROM SavingsPig s WHERE s.miku.id = :mikuId AND s.broken = false")
    long countActivePigsByMikuId(@Param("mikuId") Long mikuId);
}
