package com.example.demo.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.TestResult;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    Optional<TestResult> findTopByUserIdOrderBySubmittedAtDesc(Long userId);
}

