package com.example.applicationflow.service;

import com.example.applicationflow.exception.InvalidStatusException;
import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.model.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest()
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class ApplicationServiceIntegrationTest {

    @Autowired
    private ApplicationService applicationService;

    private final String testApp1Name = "appName1";
    private final String testApp1Content = "appContent1";
    private final String cause = "cause for delete and reject";

    @Test
    public void shouldCreateNewApplication() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).status(ApplicationStatus.CREATED).build();
        //When
        ApplicationDto result = applicationService.create(applicationToBeCreated);
        // Then
        assertNotNull(result);
        assertEquals(applicationToBeCreated.getName(), result.getName());
        assertEquals(applicationToBeCreated.getContent(), result.getContent());
        assertEquals(applicationToBeCreated.getStatus(), result.getStatus());
    }

    @Test
    public void shouldFailToCreateNewApplicationWithoutName() {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().content(testApp1Content).build();
        // When
        // Then
        DataIntegrityViolationException propertyValueException = assertThrows(DataIntegrityViolationException.class, () ->
                        applicationService.create(applicationToBeCreated),
                "not-null property references a null or transient value");

        assertTrue(propertyValueException.getMessage().contains("not-null property references a null or transient value"));
    }

    @Test
    public void shouldVerifyApplication() throws InvalidStatusException {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().id("8").name(testApp1Name).content(testApp1Content).status(ApplicationStatus.CREATED).build();
        //When
        ApplicationDto result = applicationService.verify(applicationToBeCreated);
        // Then
        assertNotNull(result);
        assertEquals(applicationToBeCreated.getName(), result.getName());
        assertEquals(applicationToBeCreated.getContent(), result.getContent());
        assertEquals(ApplicationStatus.VERIFIED, result.getStatus());
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldFailToVerifyApplicationWithWrongStatus() {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("3");
        // Then
        assertThrows(InvalidStatusException.class, () ->
                applicationService.verify(createdApplication));
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldRejectApplicationWithVerifiedStatusAndCorrectCause() throws InvalidStatusException {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("2");
        //When
        ApplicationDto result = applicationService.reject(createdApplication, cause);
        // Then
        assertNotNull(result);
        assertEquals(createdApplication.getName(), result.getName());
        assertEquals(createdApplication.getContent(), result.getContent());
        assertEquals(ApplicationStatus.REJECTED, result.getStatus());
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldFailToRejectApplicationWithouthCause() throws InvalidStatusException {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("2");
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () ->
                applicationService.reject(createdApplication, ""));
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldFindApplicationById() {
        String appId = "5";
        String appName = "appName5";
        String appContent = "appContent5";
        //When
        ApplicationDto createdApplication = applicationService.findApplicationById(appId);
        // Then
        assertNotNull(createdApplication);
        assertEquals(createdApplication.getId(), appId);
        assertEquals(createdApplication.getStatus(), ApplicationStatus.REJECTED);
        assertEquals(createdApplication.getName(), appName);
        assertEquals(createdApplication.getContent(), appContent);
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldDeleteApplicationSetsDeletedStatus() throws InvalidStatusException {
        // Given
        //When
        ApplicationDto result = applicationService.delete("1", cause);
        // Then
        assertNotNull(result);
        assertEquals(testApp1Name, result.getName());
        assertEquals(testApp1Content, result.getContent());
        assertEquals(ApplicationStatus.DELETED, result.getStatus());
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldDeleteThrowsInvalidStatusException() {
        assertThrows(IllegalArgumentException.class, () ->
                applicationService.delete("2", ""));
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldRejectThrowsInvalidStatusException() {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("2");
        // When
        // Then
        assertThrows(IllegalArgumentException.class, () ->
                applicationService.reject(createdApplication, ""));
    }

    @Test
    void shouldAcceptSetsAcceptedStatusForApplicationInVerifiedStatus() throws InvalidStatusException {
        // Given
        ApplicationDto applicationToBeCreated = ApplicationDto.builder().id("9").name(testApp1Name).content(testApp1Content).status(ApplicationStatus.VERIFIED).build();
        //When
        ApplicationDto result = applicationService.accept(applicationToBeCreated);
        // Then
        assertNotNull(result);
        assertEquals(applicationToBeCreated.getName(), result.getName());
        assertEquals(applicationToBeCreated.getContent(), result.getContent());
        assertEquals(ApplicationStatus.ACCEPTED, result.getStatus());
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldAcceptThrowsInvalidStatusException() {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("1");
        // When
        // Then
        assertThrows(InvalidStatusException.class, () ->
                applicationService.accept(createdApplication));
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldPublishSetsPublishedStatusForApplicationInPublishedStatus() throws InvalidStatusException {
        // Given
        //When
        ApplicationDto createdApplication = applicationService.findApplicationById("3");
        ApplicationDto result = applicationService.publish(createdApplication);
        // Then
        assertNotNull(result);
        assertEquals(createdApplication.getName(), result.getName());
        assertEquals(createdApplication.getContent(), result.getContent());
        assertEquals(ApplicationStatus.PUBLISHED, result.getStatus());
    }

    @Test
    @Sql(scripts = {"/CreateApplication.sql"})
    public void shouldPublishThrowsInvalidStatusException() {
        // Given
        ApplicationDto createdApplication = applicationService.findApplicationById("1");
        // When
        // Then
        assertThrows(InvalidStatusException.class, () ->
                applicationService.publish(createdApplication));
    }


}
