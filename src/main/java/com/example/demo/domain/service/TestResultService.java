package com.example.demo.domain.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.TestResult;
import com.example.demo.domain.repository.TestResultRepository;

@Service
public class TestResultService {

    @Autowired
    private TestResultRepository repo;

    // ✅ 毎回履歴として新規保存（累積正答数＋レベル計算）
    public int saveResult(Long userId, int correct, int total) {
        // 累積正答数の取得
        Optional<TestResult> latest = repo.findTopByUserIdOrderBySubmittedAtDesc(userId);
        int previousTotalCorrect = latest.map(TestResult::getTotalCorrect).orElse(0);
        int updatedTotalCorrect = previousTotalCorrect + correct;

        // レベル計算：100問ごとに1レベルアップ、Lv.1から開始
        int level = (updatedTotalCorrect / 100) + 1;

        // 保存
        TestResult result = new TestResult();
        result.setUserId(userId);
        result.setCorrect(correct);
        result.setTotal(total);
        result.setSubmittedAt(LocalDateTime.now());
        result.setTotalCorrect(updatedTotalCorrect);
        result.setLevel(level);

        repo.save(result);

        return total;
    }

    // ✅ 最新の提出結果を取得
    public TestResult getResult(Long userId) {
        return repo.findTopByUserIdOrderBySubmittedAtDesc(userId).orElse(null);
    }
}
