package com.waqiti.user.repository;

import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Find a user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find a user by phone number
     */
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    /**
     * Find a user by external ID
     */
    Optional<User> findByExternalId(String externalId);
    
    /**
     * Check if a user exists with the given username
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if a user exists with the given email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if a user exists with the given phone number
     */
    boolean existsByPhoneNumber(String phoneNumber);
    
    /**
     * Find users by status
     */
    List<User> findByStatus(UserStatus status);
    
    /**
     * Find users by username or email containing the search term
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByUsernameOrEmailContaining(@Param("searchTerm") String searchTerm);
}