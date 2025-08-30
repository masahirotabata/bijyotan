package com.example.demo.api.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.ObjectProvider;         // ★ 追加
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.api.service.UserService;
import com.example.demo.domain.entity.TestQuestion;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.entity.Word;
import com.example.demo.service.TestQuestionService;
import com.example.demo.service.WordService;

@Controller
public class HomeController {

    private final JobLauncher jobLauncher;
    private final ObjectProvider<Job> csvChunkJobProvider;  // ★ 任意注入に変更
    private final UserService userService;
    private final WordService wordService;
    private final TestQuestionService testQuestionService;

    @Autowired
    public HomeController(
            JobLauncher jobLauncher,
            @Qualifier("csvChunkJob") ObjectProvider<Job> csvChunkJobProvider, // ★ ここが肝
            UserService userService,
            WordService wordService,
            TestQuestionService testQuestionService) {
        this.jobLauncher = jobLauncher;
        this.csvChunkJobProvider = csvChunkJobProvider;
        this.userService = userService;
        this.wordService = wordService;
        this.testQuestionService = testQuestionService;
    }

    @GetMapping("/home")
    public String homePage(@RequestParam(name = "userId", required = false) String userIdParam, Model model) {
        Long userId = parseLongOrNull(userIdParam);
        UserEntity user = (userId != null) ? userService.getUserById(userId).orElse(null) : null;
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        return "home";
    }

    @PostMapping("/import")
    public ResponseEntity<String> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "userId", required = false) String userIdParam) {

        if (file.isEmpty()) return ResponseEntity.badRequest().body("CSVファイルが空です");

        Long userIdFromRequest = parseLongOrDefault(userIdParam, 0L);

        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            Set<String> headers = csvParser.getHeaderMap().keySet().stream()
                    .map(HomeController::normalizeHeader)
                    .collect(Collectors.toSet());

            Set<String> TEST_HEADERS = Set.of("sentence","audio","correct","options");
            boolean hasAllTestHeaders = TEST_HEADERS.stream().allMatch(headers::contains);
            boolean looksLikeWordCsv = headers.contains("word") || headers.contains("meaning")
                                     || headers.contains("pictdescription") || headers.contains("part");

            if (hasAllTestHeaders) {
                System.out.println("[CSV判定] TestQuestion CSV と判定。headers=" + headers);
                return importTestQuestionCsv(csvParser, userIdFromRequest);
            } else if (looksLikeWordCsv) {
                System.out.println("[CSV判定] Word CSV と判定。headers=" + headers);
                return importWordCsv(csvParser, userIdFromRequest);
            } else {
                System.out.println("[CSV判定] 判定不能。headers=" + headers);
                return ResponseEntity.badRequest().body(
                    "ヘッダーを判定できませんでした。検出ヘッダー: " + headers + "\n" +
                    "期待: TestQuestion(sentence,audio,correct,options) もしくは Word(word,meaning,...)");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSVアップロード失敗: " + e.getMessage());
        }
    }

    // ===== TestQuestion 取込 =====
    private ResponseEntity<String> importTestQuestionCsv(CSVParser csvParser, Long userIdFromRequest) {
        long ok = 0, ng = 0;

        final Map<String, Integer> headerIndex = csvParser.getHeaderMap().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> normalizeHeader(e.getKey()),
                        Map.Entry::getValue));

        int line = 1;
        for (CSVRecord rec : csvParser) {
            try {
                TestQuestion q = new TestQuestion();
                q.setSentence(getSafe(rec, headerIndex, "sentence"));
                q.setAudio(getSafe(rec, headerIndex, "audio"));
                q.setCorrect(getSafe(rec, headerIndex, "correct"));
                q.setOptions(getSafe(rec, headerIndex, "options"));

                if (hasSetterForUserId(q)) {
                    q.setUserId(userIdFromRequest != null ? userIdFromRequest : 0L);
                }

                testQuestionService.save(q);
                ok++;
            } catch (Exception ex) {
                ng++;
                System.err.println("（TestQuestion）行" + line + "でエラー: " + ex.getMessage());
            }
            line++;
        }
        return ResponseEntity.ok(String.format("TestQuestion CSVアップロード成功: OK=%d, NG=%d", ok, ng));
    }

    private static boolean hasSetterForUserId(TestQuestion q) {
        try { q.getClass().getMethod("setUserId", Long.class); return true; }
        catch (NoSuchMethodException e) {
            try { q.getClass().getMethod("setUserId", long.class); return true; }
            catch (NoSuchMethodException ignored) { return false; }
        }
    }

    // ===== Word 取込 =====
    private ResponseEntity<String> importWordCsv(CSVParser csvParser, Long userIdFromRequest) {
        long ok = 0, ng = 0;

        final Map<String, Integer> headerIndex = csvParser.getHeaderMap().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> normalizeHeader(e.getKey()),
                        Map.Entry::getValue));

        if (!headerIndex.containsKey("word")) {
            return ResponseEntity.badRequest().body(
                "Word CSV と判定されましたが必須ヘッダー 'word' が見つかりません。\n" +
                "検出ヘッダー: " + headerIndex.keySet());
        }

        int line = 1;
        for (CSVRecord record : csvParser) {
            try {
                Word w = new Word();
                w.setWord(getSafe(record, headerIndex, "word"));
                w.setMeaning(getSafe(record, headerIndex, "meaning"));
                w.setPictDescription(getSafe(record, headerIndex, "pictDescription"));
                w.setPart(getSafe(record, headerIndex, "part"));

                String statusStr = getSafe(record, headerIndex, "status");
                boolean status = statusStr != null && "true".equalsIgnoreCase(statusStr.trim());
                w.setStatus(status);

                w.setUserId(userIdFromRequest != null ? userIdFromRequest : 0L);

                if (w.getWord() == null || w.getWord().isBlank()) {
                    ng++;
                    System.err.println("（Word）行" + line + "をスキップ: 'word' が空です");
                } else {
                    wordService.saveWord(w);
                    ok++;
                }
            } catch (Exception rowEx) {
                ng++;
                System.err.println("（Word）行" + line + "でエラー: " + rowEx.getMessage());
            }
            line++;
        }

        return ResponseEntity.ok(String.format("Word CSVアップロード結果: OK=%d, NG=%d", ok, ng));
    }

    // ===== ユーティリティ =====
    private static String normalizeHeader(String h) {
        if (h == null) return null;
        String s = h.replace("\uFEFF", "");
        return s.trim().toLowerCase(Locale.ROOT);
    }
    private static Long parseLongOrNull(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return null;
        try { return Long.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
    private static Long parseLongOrDefault(String raw, long def) {
        Long v = parseLongOrNull(raw);
        return (v != null) ? v : def;
    }
    private static String getSafe(CSVRecord rec, Map<String, Integer> headerIndex, String keyWanted) {
        if (rec == null || keyWanted == null) return null;
        String normWanted = normalizeHeader(keyWanted);
        Integer idx = headerIndex.get(normWanted);
        if (idx != null) {
            try {
                String v = rec.get(idx);
                return (v != null && !v.isBlank()) ? v.trim() : null;
            } catch (Exception ignore) { return null; }
        }
        try {
            for (String realKey : rec.toMap().keySet()) {
                if (normalizeHeader(realKey).equals(normWanted)) {
                    String v = rec.get(realKey);
                    return (v != null && !v.isBlank()) ? v.trim() : null;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}
