// dto/QuizOption.java
package com.example.demo.dto;

public class QuizOption {
    public String label;       // 表示用テキスト（副詞の場合は英単語、動作の場合は説明文）
    public String imageUrl;    // 静止画像があれば（動作問題）
    public boolean correct;
}
