package com.example.demo.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class UnlockedPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getPart() {
		return part;
	}

	public void setPart(String part) {
		this.part = part;
	}

	public LocalDateTime getUnlockedAt() {
		return unlockedAt;
	}

	public void setUnlockedAt(LocalDateTime unlockedAt) {
		this.unlockedAt = unlockedAt;
	}

	private Long userId;

    private String part;

    private LocalDateTime unlockedAt;
}