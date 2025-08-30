package com.example.demo.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.UnlockedPart;

public interface UnlockedPartRepository extends JpaRepository<UnlockedPart, Long> {
    List<UnlockedPart> findByUserId(Long userId);
    boolean existsByUserIdAndPart(Long userId, String part);
}

