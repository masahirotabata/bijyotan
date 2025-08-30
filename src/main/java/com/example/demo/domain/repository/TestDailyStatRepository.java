// src/main/java/com/example/demo/domain/repository/TestDailyStatRepository.java
package com.example.demo.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.entity.TestDailyStat;

public interface TestDailyStatRepository extends JpaRepository<TestDailyStat, Long> {
  Optional<TestDailyStat> findByUserIdAndYmd(Long userId, LocalDate ymd);
  List<TestDailyStat> findByUserIdAndYmdBetweenOrderByYmd(Long userId, LocalDate from, LocalDate to);
}
