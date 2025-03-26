package com.p2pfinance.user.repository;

import com.p2pfinance.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    /**
     * Find profiles by country
     */
    List<UserProfile> findByCountry(String country);
    
    /**
     * Find profiles by preferred currency
     */
    List<UserProfile> findByPreferredCurrency(String currency);
    
    /**
     * Find profiles by preferred language
     */
    List<UserProfile> findByPreferredLanguage(String language);
    
    /**
     * Find profiles by name containing the search term
     */
    @Query("SELECT p FROM UserProfile p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<UserProfile> findByNameContaining(@Param("searchTerm") String searchTerm);
}