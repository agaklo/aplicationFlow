package com.example.applicationflow.mapper;

import com.example.applicationflow.model.Application;
import com.example.applicationflow.model.ApplicationDto;

public class ApplicationMapper {


    public static ApplicationDto mapToApplicationDto(Application application) {
        return ApplicationDto.builder()
                .id(application.getId())
                .name(application.getName())
                .content(application.getContent())
                .status(application.getStatus())
                .build();
    }

    public static Application mapToApplication(ApplicationDto applicationDto) {
        return Application.builder()
                .id(applicationDto.getId())
                .name(applicationDto.getName())
                .content(applicationDto.getContent())
                .status(applicationDto.getStatus())
                .build();
    }


}
