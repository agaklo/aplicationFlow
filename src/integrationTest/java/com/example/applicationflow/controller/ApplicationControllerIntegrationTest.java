package com.example.applicationflow.controller;

import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.model.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class ApplicationControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate template;

    private final String testApp1Name = "testAppName1";
    private final String testApp1Content = "testApp1Content1";

    @Test
    public void shouldCreateNewApplication() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).build();

        // When
        ResponseEntity<ApplicationDto> response = template.postForEntity(getUrl("/applications"), applicationToBeCreated, ApplicationDto.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto createdApplication = response.getBody();
        assertNotNull(createdApplication);
        assertEquals(testApp1Name, createdApplication.getName());
        assertEquals(testApp1Content, createdApplication.getContent());
        assertEquals(ApplicationStatus.CREATED, createdApplication.getStatus());
    }

    @Test
    public void shouldFailToCreateNewApplicationWithoutName() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().content(testApp1Content).build();

        // When
        ResponseEntity<Object> response = template.postForEntity(getUrl("/applications"), applicationToBeCreated, Object.class);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void shouldFindCreatedApplication() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).build();
        ResponseEntity<ApplicationDto> postResponse = template.postForEntity(getUrl("/applications"), applicationToBeCreated, ApplicationDto.class);
        String applicationId = postResponse.getBody().getId();

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(testApp1Name, foundApplication.getName());
        assertEquals(testApp1Content, foundApplication.getContent());
        assertEquals(ApplicationStatus.CREATED, foundApplication.getStatus());
    }

    @Test
    public void shouldNotFindUnknownApplication() {
        // Given
        String unknownApplicationId = "unknownApplicationId";

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, unknownApplicationId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void shouldFindDeletedApplicationWithCorrectStatus() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).build();
        ResponseEntity<ApplicationDto> postResponse = template.postForEntity(getUrl("/applications"), applicationToBeCreated, ApplicationDto.class);
        String applicationId = postResponse.getBody().getId();
        String cause = "cause for deletion";
        template.postForEntity(getUrl("/delete-applications/{id}"), cause, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.DELETED, foundApplication.getStatus());
    }
    private String getUrl(String path) {
        return String.format("http://localhost:%d", port).concat(path);
    }

}