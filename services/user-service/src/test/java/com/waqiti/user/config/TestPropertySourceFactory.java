// File: src/test/java/com/waqiti/user/config/TestPropertySourceFactory.java
package com.waqiti.user.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

public class TestPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties properties = loadProperties(resource.getResource());

        // Add OAuth2 required properties
        properties.setProperty("spring.security.oauth2.client.registration.google.client-id", "test-client-id");
        properties.setProperty("spring.security.oauth2.client.registration.google.client-secret", "test-client-secret");
        properties.setProperty("spring.security.oauth2.client.registration.google.scope", "profile,email");
        properties.setProperty("spring.security.oauth2.client.registration.google.redirect-uri",
                "{baseUrl}/login/oauth2/code/{registrationId}");

        String sourceName = name != null ? name : resource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName, properties);
    }

    private Properties loadProperties(Resource resource) {
        if (resource.getFilename().endsWith(".yml") || resource.getFilename().endsWith(".yaml")) {
            YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
            factoryBean.setResources(resource);
            return factoryBean.getObject();
        }

        Properties properties = new Properties();
        try {
            properties.load(resource.getInputStream());
        } catch (IOException e) {
            // Return empty properties on error
        }
        return properties;
    }
}