// com.example.demo.domain.entity.TestQuestion.java
package com.example.demo.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// com.example.demo.domain.entity.TestQuestion
@Entity
@Table(name = "TEST_QUESTION")
public class TestQuestion {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public String getSentence() {
	return sentence;
}

public void setSentence(String sentence) {
	this.sentence = sentence;
}

public String getOptions() {
	return options;
}

public void setOptions(String options) {
	this.options = options;
}

public String getCorrect() {
	return correct;
}

public void setCorrect(String correct) {
	this.correct = correct;
}

public String getAudio() {
	return audio;
}

public void setAudio(String audio) {
	this.audio = audio;
}

public Long getUserId() {
	return userId;
}

public void setUserId(Long userId) {
	this.userId = userId;
}

private String sentence;

  @Column(length = 2000)   // 選択肢を "A|B|C|D" 等で入れる想定
  private String options;

  private String correct;
  private String audio;
  private Long userId;

  @CreationTimestamp
  private LocalDateTime createdAt;

  // getter/setter …
}
