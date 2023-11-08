package com.example.applicationflow.repository;

import com.example.applicationflow.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, String> {
}
