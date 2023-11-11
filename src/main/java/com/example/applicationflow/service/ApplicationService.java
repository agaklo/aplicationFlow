package com.example.applicationflow.service;

import com.example.applicationflow.exception.InvalidStatusException;
import com.example.applicationflow.model.ApplicationDto;

import java.util.List;


public interface ApplicationService {

    ApplicationDto create(ApplicationDto applicationDto);

    ApplicationDto findApplicationById(String id);

    List<ApplicationDto> findAllApplications();

    ApplicationDto delete(String id, String cause) throws InvalidStatusException;

    ApplicationDto verify(ApplicationDto applicationDto) throws InvalidStatusException;

    ApplicationDto reject(ApplicationDto applicationDto, String cause) throws InvalidStatusException;

    ApplicationDto accept(ApplicationDto applicationDto) throws InvalidStatusException;

    ApplicationDto publish(ApplicationDto applicationDto) throws InvalidStatusException;

    ApplicationDto edit(ApplicationDto applicationDto, String content) throws InvalidStatusException;
}
