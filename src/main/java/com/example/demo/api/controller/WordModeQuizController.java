// api/controller/WordModeQuizController.java
package com.example.demo.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.service.WordModeQuizService;
import com.example.demo.dto.QuizBatchResponse;

@CrossOrigin // ブラウザ直叩き用
@RestController
@RequestMapping("/api/quiz")
public class WordModeQuizController {
    private static final Logger log = LoggerFactory.getLogger(WordModeQuizController.class);
    private final WordModeQuizService service;

    public WordModeQuizController(WordModeQuizService service) { this.service = service; }

    @GetMapping(value = "/word-mode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuizBatchResponse> get(
            @RequestParam(name = "count", defaultValue = "10") int count) {
        try {
            int safe = Math.max(1, Math.min(count, 20));
            QuizBatchResponse resp = service.generate(safe);
            if (resp == null || resp.questions == null) {
                QuizBatchResponse empty = new QuizBatchResponse();
                empty.questions = java.util.Collections.emptyList();
                return ResponseEntity.ok(empty);
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            QuizBatchResponse empty = new QuizBatchResponse();
            empty.questions = java.util.Collections.emptyList();
            return ResponseEntity.ok(empty);
        }
    }

}
