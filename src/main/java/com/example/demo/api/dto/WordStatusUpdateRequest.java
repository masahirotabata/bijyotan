package com.example.demo.api.dto;

public class WordStatusUpdateRequest {
    private Long userId;
    private boolean status;

    // Getter / Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}