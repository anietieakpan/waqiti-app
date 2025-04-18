// File: src/test/java/com/waqiti/user/config/SecurityTestContextInitializer.java
package com.waqiti.user.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;

public class SecurityTestContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.main.allow-bean-definition-overriding=true",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration"
        );
    }
}