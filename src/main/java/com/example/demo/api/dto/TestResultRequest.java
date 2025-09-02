package com.example.demo.api.dto;

import lombok.Data;

@Data
public class TestResultRequest {
    private Long userId;
    private int correct;
    private int total;
}

