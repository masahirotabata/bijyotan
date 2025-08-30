package com.example.demo.api.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;   // ← ここ（先頭）で import
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.dto.TestCommitReq;
import com.example.demo.api.dto.TestResultRequest;
import com.example.demo.domain.entity.TestDailyStat;
import com.example.demo.domain.entity.TestResult;
import com.example.demo.domain.repository.TestDailyStatRepository;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.TestResultService;

import lombok.RequiredArgsConstructor;

@CrossOrigin
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService service;
    private final UserRepository userRepository;
    private final TestDailyStatRepository dailyRepo;

    /** 簡易保存（テスト用：Map受け取り） */
    @PostMapping(path = "/save-map", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveFromMap(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(String.valueOf(body.get("userId")));
            int correct = Integer.parseInt(String.valueOf(body.get("correct")));
            int total   = Integer.parseInt(String.valueOf(body.get("total")));
            service.saveResult(userId, correct, total);
            return ResponseEntity.ok("保存完了");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("保存に失敗しました");
        }
    }

    /** 直近7日（今日を含む）の日次集計を穴埋めして返す */
    @GetMapping(path = "/weekly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> weekly(@RequestParam("userId") Long userId) {
        LocalDate to   = LocalDate.now();
        LocalDate from = to.minusDays(6);

        var rows = dailyRepo.findByUserIdAndYmdBetweenOrderByYmd(userId, from, to);

        // ymd -> [answers, correct]（null セーフ）
        Map<LocalDate, int[]> map = new HashMap<>();
        rows.forEach(r -> {
            int a = r.getAnswers();  // null チェック不要
            int c = r.getCorrect();  // null チェック不要
            map.put(r.getYmd(), new int[]{a, c});
        });

        List<Map<String, Object>> out = new ArrayList<>(7);
        for (int i = 0; i < 7; i++) {
            LocalDate d = from.plusDays(i);
            int[] v = map.getOrDefault(d, new int[]{0, 0});

            Map<String, Object> row = new HashMap<>();
            row.put("ymd", d.toString());
            row.put("answers", v[0]);
            row.put("correct", v[1]);
            out.add(row);
        }

        return ResponseEntity.ok(out);
    }

    /** 本番：正答/総数をユーザー累計に加算し、日次集計にも反映 */
    @PostMapping(path = "/commit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> commit(
            @RequestBody(required = false) TestCommitReq req,
            @RequestParam(value = "userId", required = false) Long userIdFromQuery) {

        Long userId = (req != null && req.userId() != null) ? req.userId() : userIdFromQuery;
        if (userId == null) return ResponseEntity.badRequest().build();

        final int addCorrect = (req == null) ? 0 : Math.max(0, req.correct());
        final int addTotal   = (req == null) ? 0 : Math.max(0, req.total());

     // …略…

        return userRepository.findById(userId).map(user -> {
            Integer curCorrect = user.getTestCorrectTotal();
            user.setTestCorrectTotal((curCorrect == null ? 0 : curCorrect) + addCorrect);
            userRepository.save(user);

            // 日次集計（プリミティブ想定で null チェックを外す）
            LocalDate today = LocalDate.now();
            TestDailyStat stat = dailyRepo.findByUserIdAndYmd(userId, today)
                .orElseGet(() -> {
                    TestDailyStat s = new TestDailyStat();
                    s.setUserId(userId);
                    s.setYmd(today);
                    // プリミティブなら 0 初期化は任意（念のため入れておくのは可）
                    s.setAnswers(0);
                    s.setCorrect(0);
                    return s;
                });

            int baseAnswers = stat.getAnswers(); // ← null チェック不要
            int baseCorrect = stat.getCorrect();  // ← null チェック不要
            stat.setAnswers(baseAnswers + addTotal);
            stat.setCorrect(baseCorrect + addCorrect);
            dailyRepo.save(stat);

            return ResponseEntity.ok().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());

    }

    /** レベル計算付きの既存保存 */
    @PostMapping(path = "/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> saveTestResult(@RequestBody TestResultRequest request) {
        int updatedLevel = service.saveResult(request.getUserId(), request.getCorrect(), request.getTotal());
        Map<String, Object> res = new HashMap<>();
        res.put("message", "保存成功");
        res.put("level", updatedLevel);
        return ResponseEntity.ok(res);
    }

    /** 最新結果取得 */
    @GetMapping("/result")
    public ResponseEntity<?> get(@RequestParam("userId") Long userId) {
        TestResult result = service.getResult(userId);
        return (result != null) ? ResponseEntity.ok(result) : ResponseEntity.notFound().build();
    }

    /** 動作確認 */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() { return ResponseEntity.ok("ok"); }
}
