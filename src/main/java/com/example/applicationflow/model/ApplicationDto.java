package com.example.applicationflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDto {

    private String id;
    @NotBlank
    @NotEmpty(message = "Name cannot be empty")
    private String name;
    @NotBlank
    @NotEmpty(message = "Content cannot be empty")
    private String content;
    private ApplicationStatus status;

}
