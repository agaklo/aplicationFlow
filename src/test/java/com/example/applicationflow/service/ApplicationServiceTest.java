package com.example.applicationflow.service;

import com.example.applicationflow.exception.InvalidStatusException;
import com.example.applicationflow.model.Application;
import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.model.ApplicationStatus;
import com.example.applicationflow.repository.ApplicationChangeEventRepository;
import com.example.applicationflow.repository.ApplicationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @InjectMocks
    private ApplicationServiceImpl applicationService;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationChangeEventRepository eventRepository;

    private final String testApp1Name = "testAppName1";
    private final String testApp1Content = "testAppContent1";
    private final String appId = "a379c929-dfa0-4643-9da2-6eac3e8135a1";

    @DisplayName("findAllApplications() should return all applications")
    @Test
    void shouldReturnsAllApplications() {
        // Given
        Application app1 = Application.builder().name(testApp1Name).content(testApp1Content).build();
        String testApp2Name = "testAppName2";
        String testApp2Content = "testAppContent2";
        Application app2 = Application.builder().name(testApp2Name).content(testApp2Content).build();
        when(applicationRepository.findAll()).thenReturn(List.of(app1, app2));

        // When
        List<ApplicationDto> appDtoList = applicationService.findAllApplications();

        // Then
        assertNotNull(appDtoList);
        assertThat(appDtoList).hasSize(2);
        assertEquals(appDtoList.get(0).getName(), testApp1Name);
        assertEquals(appDtoList.get(0).getContent(), testApp1Content);
        assertEquals(appDtoList.get(1).getName(), testApp2Name);
        assertEquals(appDtoList.get(1).getContent(), testApp2Content);
    }

    @DisplayName("createApplication() should set CREATED status and create an event")
    @Test
    void shouldCreateApplicationSetsCreatedStatus() {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).build();

        // When
        applicationService.create(appDto);

        // Then
        ArgumentCaptor<Application> appCaptor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository).save(appCaptor.capture());
        Application savedApp = appCaptor.getValue();
        assertEquals(savedApp.getStatus(), ApplicationStatus.CREATED);
        assertEquals(savedApp.getName(), testApp1Name);
        assertEquals(savedApp.getContent(), testApp1Content);
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.CREATED
                                && a.getApplicationId().equals(savedApp.getId())
                                && a.getName().equals(testApp1Name)
                                && a.getContent().equals(testApp1Content)
                )

        );
    }

    @DisplayName("verify() should set VERIFIED status and create an event for application in CREATED status")
    @Test
    void shouldVerifySetsVerifiedStatusForApplicationInCreatedStatus() throws InvalidStatusException {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.CREATED).name(testApp1Name).content(testApp1Content).build();
        Application app = Application.builder().id(appId).status(ApplicationStatus.VERIFIED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.save(
                        argThat(
                                a -> a.getId().equals(appId) && a.getStatus() == ApplicationStatus.VERIFIED
                        )
                )
        ).thenReturn(app);

        // When
        applicationService.verify(appDto);

        // Then
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.VERIFIED
                                && a.getApplicationId().equals(appId)
                                && a.getName().equals(testApp1Name)
                                && a.getContent().equals(testApp1Content)
                )
        );
    }

    @DisplayName("verify() should throw StatusNotFoundException for invalid application status")
    @Test
    void shouldVerifyThrowsStatusNotFoundException() {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.DELETED)
                .name(testApp1Name).content(testApp1Content).build();

        // When and Then
        assertThrows(InvalidStatusException.class, () -> applicationService.verify(appDto));
    }

    @DisplayName("findApplicationById() should return application by id")
    @Test
    void shouldFindApplicationById() {
        // Given
        Application app = Application.builder().id(appId).status(ApplicationStatus.VERIFIED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));

        // When
        ApplicationDto appDto = applicationService.findApplicationById(appId);

        // Then
        assertNotNull(appDto);
        assertEquals(appDto.getId(), appId);
        assertEquals(appDto.getStatus(), ApplicationStatus.VERIFIED);
        assertEquals(appDto.getName(), testApp1Name);
        assertEquals(appDto.getContent(), testApp1Content);
    }


    @DisplayName("delete should set DELETED status and create an event")
    @Test
    void shouldDeleteApplicationSetsDeletedStatus() throws InvalidStatusException {
        // Given
        Application app = Application.builder().id(appId).status(ApplicationStatus.CREATED).name(testApp1Name).content(testApp1Content).build();
        Application appForSave = Application.builder().id(appId).status(ApplicationStatus.DELETED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.save(
                        argThat(
                                a -> a.getId().equals(appId) && a.getStatus() == ApplicationStatus.DELETED
                        )
                )
        ).thenReturn(appForSave);
        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));

        // When
        applicationService.delete(appId, "test cause");

        // Then
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.DELETED
                                && a.getApplicationId().equals(appId)
                                && a.getName().equals(testApp1Name)
                )
        );
    }


    @DisplayName("delete() should throw StatusNotFoundException if application status not CREATED")
    @Test
    void shouldDeleteThrowsStatusNotFoundException() {
        // Given
        Application app = Application.builder().id(appId).status(ApplicationStatus.DELETED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.findById(appId)).thenReturn(java.util.Optional.of(app));

        //Then
        assertThrows(InvalidStatusException.class, () -> applicationService.delete(appId, "test cause"));
    }


    @DisplayName("reject() should set Status REJECTED and create an event for application in accepted status")
    @Test
    void shouldRejectSetsRejectedStatusForApplicationInCreatedStatus() throws InvalidStatusException {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.VERIFIED).name(testApp1Name).content(testApp1Content).build();
        Application app = Application.builder().id(appId).status(ApplicationStatus.REJECTED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.save(
                        argThat(
                                a -> a.getId().equals(appId) && a.getStatus() == ApplicationStatus.REJECTED
                        )
                )
        ).thenReturn(app);

        // When
        applicationService.reject(appDto, "test cause");

        // Then
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.REJECTED
                                && a.getApplicationId().equals(appId)
                                && a.getName().equals(testApp1Name)
                )
        );
    }

    @DisplayName("reject() should throw InvalidStatusException if application status not Verified and not accepted")
    @Test
    void shouldRejectThrowsInvalidStatusException() {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.DELETED).name(testApp1Name).content(testApp1Content).build();

        // When and Then
        assertThrows(InvalidStatusException.class, () -> applicationService.reject(appDto, "test cause"));
    }


    @DisplayName("accept() should set Status ACCEPTED and create an event for application in accepted status")
    @Test
    void shouldAcceptSetsAcceptedStatusForApplicationInVerifiedStatus() throws InvalidStatusException {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.VERIFIED).name(testApp1Name).content(testApp1Content).build();
        Application app = Application.builder().id(appId).status(ApplicationStatus.ACCEPTED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.save(
                        argThat(
                                a -> a.getId().equals(appId) && a.getStatus() == ApplicationStatus.ACCEPTED
                        )
                )
        ).thenReturn(app);

        // When
        applicationService.accept(appDto);

        // Then
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.ACCEPTED
                                && a.getApplicationId().equals(appId)
                                && a.getName().equals(testApp1Name)
                )
        );
    }


    @DisplayName("accept() should throw StatusNotFoundException if application status not Verified")
    @Test
    void shouldAcceptThrowsStatusNotFoundException() {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.DELETED).name(testApp1Name).content(testApp1Content).build();

        // When and Then
        assertThrows(InvalidStatusException.class, () -> applicationService.accept(appDto));
    }


    @DisplayName("publish() should throw StatusNotFoundException if application status not Accepted")
    @Test
    void shouldPublishThrowsStatusNotFoundException() {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.DELETED).name(testApp1Name).content(testApp1Content).build();

        // When and Then
        assertThrows(InvalidStatusException.class, () -> applicationService.publish(appDto));
    }


    @DisplayName("publish should set Status Published nd create an event for application in Published status")
    @Test
    void shouldPublishSetsPublishedStatusForApplicationInPublishedStatus() throws InvalidStatusException {
        // Given
        ApplicationDto appDto = ApplicationDto.builder().id(appId).status(ApplicationStatus.ACCEPTED).name(testApp1Name).content(testApp1Content).build();
        Application app = Application.builder().id(appId).status(ApplicationStatus.PUBLISHED).name(testApp1Name).content(testApp1Content).build();
        when(applicationRepository.save(
                        argThat(
                                a -> a.getId().equals(appId) && a.getStatus() == ApplicationStatus.PUBLISHED
                        )
                )
        ).thenReturn(app);

        // When
        applicationService.publish(appDto);

        // Then
        verify(eventRepository).save(
                argThat(
                        a -> a.getStatus() == ApplicationStatus.PUBLISHED
                                && a.getApplicationId().equals(appId)
                                && a.getName().equals(testApp1Name)
                )
        );

    }

}