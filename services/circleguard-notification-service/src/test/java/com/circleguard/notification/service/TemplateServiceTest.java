package com.circleguard.notification.service;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class TemplateServiceTest {

    private final TemplateService templateService = createTemplateService();

    @Test
    void testEmailTemplateGeneration() {
        String content = templateService.generateEmailContent("SUSPECT", "John Doe");
        assertThat(content).contains("John Doe");
        assertThat(content).contains("isolation guidelines");
        assertThat(content).contains("Testing Schedule");
    }

    @Test
    void testPushTemplateGeneration() {
        String content = templateService.generatePushContent("PROBABLE");
        assertThat(content).contains("Monitor symptoms");
    }

    @Test
    void testPushMetadataGeneration() {
        var metadata = templateService.generatePushMetadata("SUSPECT");
        assertThat(metadata).containsEntry("url", "circleguard://guidelines");
        
        var emptyMetadata = templateService.generatePushMetadata("OTHER");
        assertThat(emptyMetadata).isEmpty();
    }

    @Test
    void testSmsTemplateGeneration() {
        String content = templateService.generateSmsContent("SUSPECT");
        assertThat(content).contains("SUSPECT");
        assertThat(content).contains("check your email");
    }

    private TemplateService createTemplateService() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassLoaderForTemplateLoading(getClass().getClassLoader(), "templates");

        TemplateService service = new TemplateService(configuration);
        ReflectionTestUtils.setField(service, "testingUrl", "https://circleguard.example.com/testing");
        ReflectionTestUtils.setField(service, "isolationUrl", "https://circleguard.example.com/isolation");
        ReflectionTestUtils.setField(service, "guidelinesDeepLink", "circleguard://guidelines");
        return service;
    }
}
