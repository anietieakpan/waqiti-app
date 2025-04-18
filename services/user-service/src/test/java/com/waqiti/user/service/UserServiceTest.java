// File: user-service/src/test/java/com/waqiti/user/service/UserServiceTest.java
package com.waqiti.user.service;

import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserProfile;
import com.waqiti.user.dto.UserRegistrationRequest;
import com.waqiti.user.dto.UserResponse;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.repository.VerificationTokenRepository;
import com.waqiti.user.client.IntegrationServiceClient;
import com.waqiti.user.client.dto.CreateUserResponse;
import com.waqiti.user.domain.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IntegrationServiceClient integrationClient;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("Password123");
        registrationRequest.setPhoneNumber("+1234567890");
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        String externalId = "ext-123";
        String encodedPassword = "encoded-password";

        CreateUserResponse integrationResponse = new CreateUserResponse();
        integrationResponse.setExternalId(externalId);

        // Create real domain objects for the test
        User savedUser = User.create(
                registrationRequest.getUsername(),
                registrationRequest.getEmail(),
                encodedPassword,
                externalId
        );
        savedUser.setId(userId);

        UserProfile savedProfile = UserProfile.create(savedUser);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(integrationClient.createUser(any())).thenReturn(integrationResponse);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));
        when(profileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
        // Important: make sure VerificationToken creation doesn't fail
        when(tokenRepository.save(any())).thenReturn(null); // Return value not used

        // Act
        UserResponse response = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(integrationClient).createUser(any());
        verify(userRepository).save(any(User.class));
        verify(profileRepository).save(any(UserProfile.class));
        verify(tokenRepository).save(any());
    }

    @Test
    void testGetUserById_Success() {
        // Arrange
        User user = User.create("testuser", "test@example.com", "passwordHash", "externalId");
        user.setId(userId);
        UserProfile profile = UserProfile.create(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("testuser", response.getUsername());

        verify(userRepository).findById(userId);
        verify(profileRepository).findById(userId);
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(nonExistentId));

        verify(userRepository).findById(nonExistentId);
        verifyNoInteractions(profileRepository);
    }
}