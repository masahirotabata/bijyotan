package com.example.demo.api.dto;

public record TestCommitReq(Long userId, String mode, int correct, int total) {}
