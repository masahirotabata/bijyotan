package com.example.demo.api.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.entity.TestQuestion;
import com.example.demo.domain.repository.TestQuestionRepository;
import com.example.demo.domain.repository.WordRepository;
import com.example.demo.service.TestQuestionService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/test-questions")
public class TestQuestionController {
    private final TestQuestionService service;
    private final TestQuestionRepository repo;
    private final WordRepository wordRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public TestQuestionController(TestQuestionService service,
                                  TestQuestionRepository repo,
                                  WordRepository wordRepo) {
        this.service = service;
        this.repo = repo;
        this.wordRepo = wordRepo;
    }

    @GetMapping("/play")
    public List<Map<String, Object>> play(@RequestParam("userId") Long userId) {
        // 通常取得（userIdに紐づく問題）
        List<TestQuestion> questions = repo.findByUserId(userId);

        // 該当がなければ userId=1 のデフォルト問題を代用
        if (questions.isEmpty() && userId != 1) {
            questions = repo.findByUserId(1L);
        }

        return questions.stream().map(q -> {
            List<String> options;
            String opts = q.getOptions();
            if (opts == null || opts.isBlank()) {
                options = List.of();
            } else if (opts.trim().startsWith("[")) {
                try {
                    options = mapper.readValue(opts, new TypeReference<List<String>>() {});
                } catch (Exception e) {
                    options = Arrays.stream(opts.split("\\|")).map(String::trim).toList();
                }
            } else {
                options = Arrays.stream(opts.split("\\|")).map(String::trim).toList();
            }

            String correct = q.getCorrect();
            String correctPictUrl = null;
            String correctJa = null;

            if (correct != null && !correct.isBlank()) {
                var w = wordRepo.findByWordIgnoreCase(correct).orElse(null);
                if (w != null) {
                    correctPictUrl = w.getPictUrlStatic();
                    correctJa = w.getMeaning();
                }
            }

            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("sentence",       q.getSentence() != null ? q.getSentence() : "");
            m.put("audio",          q.getAudio() != null ? q.getAudio() : "");
            m.put("correct",        correct != null ? correct : "");
            m.put("correctPictUrl", correctPictUrl);
            m.put("correctJa",      correctJa);
            m.put("options",        options != null ? options : List.of());
            return m;
        }).toList();
    }

}
