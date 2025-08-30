// dto/QuizQuestion.java
package com.example.demo.dto;

import java.util.List;

import com.example.demo.dto.QuizOption;

public class QuizQuestion {
    public String id;             // word.id 等（文字列にしておくと楽）
    public String questionType;   // "action" or "adverb"
    public String prompt;         // 画面上に出す文面：action=英単語(word)/adverb=日本語(meaning)
    public String word;           // 英単語（採点や結果画面で利用）
    public String part;           // 品詞（adverb/verb/...）
    public List<QuizOption> options;
}
