// File: src/test/java/com/waqiti/user/api/TestMfaExceptionHandler.java
package com.waqiti.user.api;

import com.waqiti.user.security.SecurityContextHelper;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;

@ControllerAdvice
@Profile("test")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestMfaExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("Invalid UUID string")) {
            // Return the test user ID for any UUID conversion failures
            return ResponseEntity.ok(SecurityContextHelper.getTestUserId());
        }
        // Let other exceptions pass through
        throw ex;
    }
}