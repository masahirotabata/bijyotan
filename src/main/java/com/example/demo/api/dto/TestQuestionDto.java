package com.example.demo.api.dto;

public class TestQuestionDto {
    public String sentence;
    public String correct;
    public String correctPictUrl;
    public String options; // 例：カンマ区切り or JSON
    public String audio;
    public String ja;      // ← 追加
}