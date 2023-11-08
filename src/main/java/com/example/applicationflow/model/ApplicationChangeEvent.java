package com.example.applicationflow.model;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.Instant;

@Entity
@Table(name = "events")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ApplicationChangeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String eventId;
    @Column(nullable = false, length = 36)
    private String applicationId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String content;
    private ApplicationStatus status;
    private Instant timestamp;
    @NotEmpty
    @NotBlank
    private String cause;

}
