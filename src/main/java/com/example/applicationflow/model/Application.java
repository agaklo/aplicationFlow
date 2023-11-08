package com.example.applicationflow.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class Application {

    @Id
    @Column(nullable = false, length = 36)
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String content;
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

}
