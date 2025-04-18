/**
 * File: src/test/java/com/waqiti/notification/service/TemplateRendererTest.java
 */
package com.waqiti.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class TemplateRendererTest {

    @Mock
    private ITemplateEngine templateEngine;

    private TemplateRenderer templateRenderer;

    @BeforeEach
    void setUp() {
        templateRenderer = new TemplateRenderer(templateEngine);
    }

    @Test
    void renderTemplate_ShouldRenderTemplateWithMapModel() {
        // Given
        String template = "Hello ${username}, your balance is ${amount}";
        String expectedRenderedTemplate = "Hello John, your balance is $100.00";

        Map<String, Object> model = new HashMap<>();
        model.put("username", "John");
        model.put("amount", "$100.00");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedRenderedTemplate);

        // When
        String result = templateRenderer.renderTemplate(template, model);

        // Then
        assertThat(result).isEqualTo(expectedRenderedTemplate);
        verify(templateEngine).process(eq("<div th:remove=\"tag\">" + template + "</div>"), any(Context.class));
    }

    @Test
    void renderTemplate_ShouldRenderTemplateWithObjectModel() {
        // Given
        String template = "Hello ${model.username}, your balance is ${model.amount}";
        String expectedRenderedTemplate = "Hello John, your balance is $100.00";

        TestModel model = new TestModel("John", "$100.00");

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedRenderedTemplate);

        // When
        String result = templateRenderer.renderTemplate(template, model);

        // Then
        assertThat(result).isEqualTo(expectedRenderedTemplate);
        verify(templateEngine).process(eq("<div th:remove=\"tag\">" + template + "</div>"), any(Context.class));
    }

    @Test
    void renderTemplate_ShouldReturnOriginalTemplate_WhenRenderingFails() {
        // Given
        String template = "Hello ${username}";
        Map<String, Object> model = Map.of("username", "John");

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Rendering error"));

        // When
        String result = templateRenderer.renderTemplate(template, model);

        // Then
        assertThat(result).isEqualTo(template);
    }

    @Test
    void renderTemplate_ShouldHandleNullModel() {
        // Given
        String template = "Hello world";
        String expectedRenderedTemplate = "Hello world";

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedRenderedTemplate);

        // When
        String result = templateRenderer.renderTemplate(template, null);

        // Then
        assertThat(result).isEqualTo(expectedRenderedTemplate);
        verify(templateEngine).process(eq("<div th:remove=\"tag\">" + template + "</div>"), any(Context.class));
    }

    @Test
    void renderTemplate_ShouldHandleEmptyTemplate() {
        // Given
        String template = "";
        String expectedRenderedTemplate = "";

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(expectedRenderedTemplate);

        // When
        String result = templateRenderer.renderTemplate(template, Map.of());

        // Then
        assertThat(result).isEqualTo(expectedRenderedTemplate);
        verify(templateEngine).process(eq("<div th:remove=\"tag\"></div>"), any(Context.class));
    }

    // Helper class for object model test
    private static class TestModel {
        private final String username;
        private final String amount;

        public TestModel(String username, String amount) {
            this.username = username;
            this.amount = amount;
        }

        public String getUsername() {
            return username;
        }

        public String getAmount() {
            return amount;
        }
    }
}