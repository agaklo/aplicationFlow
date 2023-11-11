package com.example.applicationflow.service;

import com.example.applicationflow.model.ApplicationStatus;
import com.example.applicationflow.exception.InvalidStatusException;
import com.example.applicationflow.mapper.ApplicationChangeEventMapper;
import com.example.applicationflow.mapper.ApplicationMapper;
import com.example.applicationflow.model.Application;
import com.example.applicationflow.model.ApplicationChangeEvent;
import com.example.applicationflow.model.ApplicationDto;
import com.example.applicationflow.repository.ApplicationChangeEventRepository;
import com.example.applicationflow.repository.ApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationChangeEventRepository eventRepository;


    public ApplicationServiceImpl(ApplicationRepository applicationRepository, ApplicationChangeEventRepository eventRepository) {
        this.applicationRepository = applicationRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public ApplicationDto create(ApplicationDto applicationDto) {
        Application application = ApplicationMapper.mapToApplication(applicationDto);
        application.setStatus(ApplicationStatus.CREATED);
        application.setId(UUID.randomUUID().toString());
        applicationRepository.save(application);
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, null);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public List<ApplicationDto> findAllApplications() {
        return applicationRepository.findAll()
                .stream()
                .map(ApplicationMapper::mapToApplicationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ApplicationDto findApplicationById(String id) {
        return applicationRepository.findById(id).map(ApplicationMapper::mapToApplicationDto).orElse(null);
    }

    @Override
    public ApplicationDto delete(String id, String cause) throws InvalidStatusException {
        ApplicationDto applicationDto = findApplicationById(id);
        if (applicationDto == null) {
            throw new IllegalArgumentException("Application not found");
        }
        if (cause == null || cause.isBlank()) {
            throw new IllegalArgumentException("Cause is required");
        }
        if (ApplicationStatus.CREATED != applicationDto.getStatus()) {
            throw new InvalidStatusException(applicationDto.getStatus().toString());
        }
        applicationDto.setStatus(ApplicationStatus.DELETED);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, cause);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public ApplicationDto verify(ApplicationDto applicationDto) throws InvalidStatusException {
        if (applicationDto.getStatus() != ApplicationStatus.CREATED) {
            throw new InvalidStatusException("Invalid application status:" + applicationDto.getStatus().toString());
        }
        applicationDto.setStatus(ApplicationStatus.VERIFIED);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, null);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public ApplicationDto reject(ApplicationDto applicationDto, String cause) throws InvalidStatusException {
        if (cause == null || cause.isBlank()) {
            throw new IllegalArgumentException("Cause is required");
        }
        if (applicationDto.getStatus() != ApplicationStatus.VERIFIED
                && applicationDto.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidStatusException("Invalid application status:" + applicationDto.getStatus().toString());
        }
        applicationDto.setStatus(ApplicationStatus.REJECTED);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, cause);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public ApplicationDto accept(ApplicationDto applicationDto) throws InvalidStatusException {
        if (applicationDto.getStatus() != ApplicationStatus.VERIFIED) {
            throw new InvalidStatusException("Invalid application status:" + applicationDto.getStatus().toString());
        }
        applicationDto.setStatus(ApplicationStatus.ACCEPTED);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, null);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public ApplicationDto publish(ApplicationDto applicationDto) throws InvalidStatusException {
        if (applicationDto.getStatus() != ApplicationStatus.ACCEPTED) {
            throw new InvalidStatusException("Invalid application status:" + applicationDto.getStatus().toString());
        }
        applicationDto.setStatus(ApplicationStatus.PUBLISHED);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, null);

        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

    @Override
    public ApplicationDto edit(ApplicationDto applicationDto, String content) throws InvalidStatusException{
        if (applicationDto.getStatus() != ApplicationStatus.VERIFIED
                && applicationDto.getStatus() != ApplicationStatus.CREATED){
            throw new InvalidStatusException("Invalid application status:" + applicationDto.getStatus().toString());
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        applicationDto.setContent(content);
        Application application = applicationRepository.save(ApplicationMapper.mapToApplication(applicationDto));
        ApplicationChangeEvent event = ApplicationChangeEventMapper.mapToApplicationEvent(application, null);
        eventRepository.save(event);
        return ApplicationMapper.mapToApplicationDto(application);
    }

}

