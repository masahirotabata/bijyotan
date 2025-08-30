package com.example.demo.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.domain.entity.LearningStatus;

@Repository
public interface LearningStatusRepository extends JpaRepository<LearningStatus, Long> {

    Optional<LearningStatus> findByUserIdAndWordId(Long userId, Long wordId);

    Optional<LearningStatus> findByUserIdAndWordIdAndPart(Long userId, Long wordId, String part);

    int countByUserIdAndPartAndLearnedTrue(Long userId, String part);
}
