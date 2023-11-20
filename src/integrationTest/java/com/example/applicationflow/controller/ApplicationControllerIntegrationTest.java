package com.example.applicationflow.controller;

import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.model.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    private final String cause = "cause for delete and reject";

    @Test
    public void shouldCreateNewApplication() {
        // When
        ResponseEntity<ApplicationDto> response = createApplicationForTest();

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
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();

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
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/delete-application/{id}"), cause, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.DELETED, foundApplication.getStatus());
    }

    @Test
    public void shouldFindRejectedApplicationWithCorrectStatus() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/verify-application/{id}"), null, String.class, applicationId);
        template.postForEntity(getUrl("/reject-application/{id}"), cause, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.REJECTED, foundApplication.getStatus());
    }

    @Test
    public void shouldVerifyApplicationWithCreatedStatus() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/verify-application/{id}"), null, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.VERIFIED, foundApplication.getStatus());
    }

    @Test
    public void shouldEditApplicationWithCreatedStatus() {
        //Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();

        //When
        String newContent = "newContent";
        HttpEntity<String> requestEntity = new HttpEntity<>(newContent);
        ResponseEntity<ApplicationDto> response = template.exchange(getUrl("/applications/{id}/content"), HttpMethod.PUT, requestEntity, ApplicationDto.class, applicationId);

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(newContent, response.getBody().getContent());
    }

    @Test
    public void shouldFailWhenEditApplicationWithoutContent() {
        //Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();

        //When
        HttpEntity<String> requestEntity = new HttpEntity<>("");
        ResponseEntity<?> response = template.exchange(getUrl("/applications/{id}/content"), HttpMethod.PUT, requestEntity, Object.class, applicationId);

        //Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    public void shouldAcceptApplicationWithVerifiedStatus() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/verify-application/{id}"), null, String.class, applicationId);
        template.postForEntity(getUrl("/accept-application/{id}"), null, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.ACCEPTED, foundApplication.getStatus());
    }

    @Test
    public void shouldRejectApplicationWithCorrectStatus() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/verify-application/{id}"), null, String.class, applicationId);
        template.postForEntity(getUrl("/reject-application/{id}"), cause, String.class, applicationId);
        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.REJECTED, foundApplication.getStatus());
    }

    @Test
    public void shouldFindApplicationList() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();

        // When
        ResponseEntity<Object> response = template.postForEntity(getUrl("/applications"), applicationCreated, Object.class);

        //Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldPublishApplicationWithCorrectStatus() {
        // Given
        ResponseEntity<ApplicationDto> applicationCreated = createApplicationForTest();
        String applicationId = applicationCreated.getBody().getId();
        template.postForEntity(getUrl("/verify-application/{id}"), null, String.class, applicationId);
        template.postForEntity(getUrl("/accept-application/{id}"), null, String.class, applicationId);
        template.postForEntity(getUrl("/publish-application/{id}"), null, String.class, applicationId);

        // When
        ResponseEntity<ApplicationDto> response = template.getForEntity(getUrl("/applications/{id}"), ApplicationDto.class, applicationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApplicationDto foundApplication = response.getBody();
        assertNotNull(foundApplication);
        assertEquals(applicationId, foundApplication.getId());
        assertEquals(ApplicationStatus.PUBLISHED, foundApplication.getStatus());
    }

    private ResponseEntity<ApplicationDto> createApplicationForTest() {
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).status(ApplicationStatus.CREATED).build();
        return template.postForEntity(getUrl("/applications"), applicationToBeCreated, ApplicationDto.class);
    }

    private String getUrl(String path) {
        return String.format("http://localhost:%d", port).concat(path);
    }

}