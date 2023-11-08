package com.example.applicationflow.repository;

import com.example.applicationflow.model.ApplicationChangeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationChangeEventRepository extends JpaRepository<ApplicationChangeEvent, String> {
}
