package com.example.demo.api.controller;  // ★これを先頭に必ず入れる

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.api.dto.WordDto;
import com.example.demo.domain.entity.LearningStatus;
import com.example.demo.domain.entity.Word;
import com.example.demo.domain.repository.LearningStatusRepository;
import com.example.demo.domain.repository.WordRepository;
import com.example.demo.service.WordService;

@RestController
@RequestMapping("/api/words")
public class WordController {

    private final WordService wordService;
    private final WordRepository wordRepository;
    private final LearningStatusRepository learningStatusRepository;

    @Autowired
    public WordController(WordService wordService,
                          WordRepository wordRepository,
                          LearningStatusRepository learningStatusRepository) {
        this.wordService = wordService;
        this.wordRepository = wordRepository;
        this.learningStatusRepository = learningStatusRepository;
    }

    @GetMapping("/withStatus")
    public List<WordDto> getWordsWithStatus(@RequestParam("userId") Long userId,
                                            @RequestParam("part") String part) {
        List<Word> words = wordRepository.findByUserIdAndPart(userId, part);
        return words.stream().map(word -> {
            boolean status = learningStatusRepository
                .findByUserIdAndWordId(userId, word.getId())
                .map(LearningStatus::isLearned)
                .orElse(false);
            return new WordDto(word, status);
        }).collect(Collectors.toList());
    }

    @GetMapping
    public ResponseEntity<List<WordDto>> getWords(
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam("part") String part) {
        if (userId == null) return ResponseEntity.ok(List.of());
        return ResponseEntity.ok(wordService.getWordsForUser(userId, part));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id) {
        Word word = wordService.findById(id);
        if (word == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Word not found");
        word.setStatus(!word.isStatus());
        wordService.saveWord(word);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateWordStatus(@PathVariable Long id) {
        return wordService.updateStatusOk(id) ? ResponseEntity.noContent().build()
                                              : ResponseEntity.notFound().build();
    }

    // ★ ここを "/import" に修正
    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file,
                                            @RequestParam(name = "userId", required = false) Long userId) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("CSVファイルが空です");

        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            Set<String> headers = csvParser.getHeaderMap().keySet().stream()
                    .map(this::normalizeHeader).collect(Collectors.toSet());

            Set<String> TEST_HEADERS = Set.of("sentence", "audio", "correct", "options");
            if (TEST_HEADERS.stream().allMatch(headers::contains)) {
                return ResponseEntity.badRequest().body(
                    "このCSVは TestQuestion 形式のようです（headers=" + headers + "）。\n" +
                    "アップロード先を /api/test-questions/import に変更してください。");
            }
            if (!headers.contains("word")) {
                return ResponseEntity.badRequest().body(
                    "Word CSV と判定されましたが必須ヘッダー 'word' が見つかりません。\n" +
                    "検出ヘッダー: " + headers);
            }

            long ok = 0, ng = 0;
            final Map<String, Integer> headerIndex = csvParser.getHeaderMap().entrySet().stream()
                    .collect(Collectors.toMap(e -> normalizeHeader(e.getKey()), Map.Entry::getValue));

            int line = 1;
            for (CSVRecord rec : csvParser) {
                try {
                    Word w = new Word();
                    w.setWord(getSafe(rec, headerIndex, "word"));
                    w.setMeaning(getSafe(rec, headerIndex, "meaning"));
                    w.setPictDescription(getSafe(rec, headerIndex, "pictDescription"));
                    w.setPart(getSafe(rec, headerIndex, "part"));
                    String statusStr = getSafe(rec, headerIndex, "status");
                    w.setStatus(statusStr != null && "true".equalsIgnoreCase(statusStr.trim()));
                    if (userId != null) w.setUserId(userId);

                    if (w.getWord() == null || w.getWord().isBlank()) {
                        ng++; System.err.println("（Word）行" + line + "をスキップ: 'word' が空です");
                    } else {
                        wordService.saveWord(w); ok++;
                    }
                } catch (Exception ex) {
                    ng++; System.err.println("（Word）行" + line + "でエラー: " + ex.getMessage());
                }
                line++;
            }
            return ResponseEntity.ok(String.format("Word CSVアップロード結果: OK=%d, NG=%d", ok, ng));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("CSVアップロード失敗: " + e.getMessage());
        }
    }

    // utils
    private String normalizeHeader(String h) {
        if (h == null) return null;
        String s = h.replace("\uFEFF", "");
        return s.trim().toLowerCase(Locale.ROOT);
    }
    private String getSafe(CSVRecord rec, Map<String,Integer> headerIndex, String wanted) {
        if (rec == null || wanted == null) return null;
        String norm = normalizeHeader(wanted);
        Integer idx = headerIndex.get(norm);
        if (idx != null) {
            try { String v = rec.get(idx); return (v != null && !v.isBlank()) ? v.trim() : null; }
            catch (Exception ignore) { return null; }
        }
        try {
            for (String realKey : rec.toMap().keySet()) {
                if (normalizeHeader(realKey).equals(norm)) {
                    String v = rec.get(realKey);
                    return (v != null && !v.isBlank()) ? v.trim() : null;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }
}
