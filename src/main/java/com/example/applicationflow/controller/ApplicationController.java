package com.example.applicationflow.controller;

import com.example.applicationflow.service.ApplicationService;
import com.example.applicationflow.exception.InvalidStatusException;
import com.example.applicationflow.model.ApplicationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/applications")
    public List<ApplicationDto> showApplicationList() {
        return applicationService.findAllApplications();
    }

    @PostMapping("/applications")
    public ApplicationDto createApplication(@Valid @RequestBody ApplicationDto applicationDto) {
        return applicationService.create(applicationDto);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationDto> getApplication(@PathVariable("id") String id) {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationDto);
    }

    @PutMapping("/applications/{id}/content")
    public ResponseEntity<ApplicationDto> editApplicationContent(@PathVariable("id") String id, @Valid @RequestBody String content) throws InvalidStatusException{
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.edit(applicationDto, content));
    }

    @PostMapping("/verify-application/{id}")
    public ResponseEntity<ApplicationDto> verifyApplication(@PathVariable("id") String id) throws InvalidStatusException {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.verify(applicationDto));
    }

    @PostMapping("/reject-application/{id}")
    public ResponseEntity<ApplicationDto> rejectApplication(@PathVariable("id") String id,
                                                            @Valid @RequestBody String cause) throws InvalidStatusException {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.reject(applicationDto, cause));
    }

    @PostMapping("/accept-application/{id}")
    public ResponseEntity<ApplicationDto> acceptApplication(@PathVariable("id") String id) throws InvalidStatusException {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.accept(applicationDto));
    }

    @PostMapping("/delete-application/{id}")
    public ResponseEntity<ApplicationDto> deleteApplication(@PathVariable("id") String id, @Valid @RequestBody String cause) throws InvalidStatusException {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.delete(applicationDto.getId(), cause));
    }

    @PostMapping("/publish-application/{id}")
    public ResponseEntity<ApplicationDto> publishApplication(@PathVariable("id") String id) throws InvalidStatusException {
        ApplicationDto applicationDto = applicationService.findApplicationById(id);
        if (applicationDto == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(applicationService.publish(applicationDto));
    }

}
