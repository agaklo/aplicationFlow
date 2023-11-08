package com.example.applicationflow.mapper;

import com.example.applicationflow.model.Application;
import com.example.applicationflow.model.ApplicationChangeEvent;

import java.time.Instant;

public class ApplicationChangeEventMapper {


    public static ApplicationChangeEvent mapToApplicationEvent(Application application, String cause) {
        return ApplicationChangeEvent.builder()
                .applicationId(application.getId())
                .name(application.getName())
                .content(application.getContent())
                .status(application.getStatus())
                .cause(cause)
                .timestamp(Instant.now())
                .build();
    }

}
