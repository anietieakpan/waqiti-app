// File: src/test/java/com/waqiti/user/config/SecurityBeanPostProcessor.java
package com.waqiti.user.config;

import com.waqiti.user.security.JwtAuthenticationFilter;
import com.waqiti.user.security.TestJwtAuthenticationFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityBeanPostProcessor implements BeanPostProcessor {

    private final TestJwtAuthenticationFilter testFilter;

    public SecurityBeanPostProcessor(TestJwtAuthenticationFilter testFilter) {
        this.testFilter = testFilter;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof JwtAuthenticationFilter) {
            // Replace with our test filter
            return testFilter;
        }
        return bean;
    }
}