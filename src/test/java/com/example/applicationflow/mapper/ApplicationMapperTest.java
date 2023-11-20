package com.example.applicationflow.mapper;

import com.example.applicationflow.model.Application;
import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.model.ApplicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class ApplicationMapperTest {


    private final String testApp1Name = "testAppName1";
    private final String testApp1Content = "testAppContent1";


    @DisplayName("should return ApplicationDto object")
    @Test
    void shouldMapToApplicationDto() {
        //given
        Application app1 = Application.builder().name(testApp1Name).content(testApp1Content).status(ApplicationStatus.CREATED).build();

        //when
        ApplicationDto applicationDto = ApplicationMapper.mapToApplicationDto(app1);

        //then
        assertNotNull(applicationDto);
        assertEquals(applicationDto.getStatus(), ApplicationStatus.CREATED);
        assertEquals(applicationDto.getName(), testApp1Name);
        assertEquals(applicationDto.getContent(), testApp1Content);
    }

    @DisplayName("should return Application object")
    @Test
    void shouldMapToApplication() {
        //given
        ApplicationDto applicationDto = ApplicationDto.builder().name(testApp1Name).content(testApp1Content).status(ApplicationStatus.CREATED).build();

        //when
        Application application = ApplicationMapper.mapToApplication(applicationDto);

        //then
        assertNotNull(application);
        assertEquals(application.getStatus(), ApplicationStatus.CREATED);
        assertEquals(application.getName(), testApp1Name);
        assertEquals(application.getContent(), testApp1Content);
    }

}
