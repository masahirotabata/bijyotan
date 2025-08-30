// src/main/java/com/example/demo/domain/entity/TestDailyStat.java
package com.example.demo.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "test_daily_stat",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","ymd"}))
public class TestDailyStat {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name="user_id", nullable=false)
  private Long userId;

  @Column(name="ymd", nullable=false)
  private LocalDate ymd;

  @Column(name="answers", nullable=false)
  private int answers;   // その日の解答数（= total）

  @Column(name="correct", nullable=false)
  private int correct;   // その日の正解数

  // getters/setters
  public Long getId() { return id; }
  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public LocalDate getYmd() { return ymd; }
  public void setYmd(LocalDate ymd) { this.ymd = ymd; }
  public int getAnswers() { return answers; }
  public void setAnswers(int answers) { this.answers = answers; }
  public int getCorrect() { return correct; }
  public void setCorrect(int correct) { this.correct = correct; }
}
