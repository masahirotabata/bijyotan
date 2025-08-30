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
import com.example.demo.domain.entity.TestQuestion;          // ★ 追加（エンティティ名はプロジェクトに合わせてください）
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.entity.Word;
import com.example.demo.service.TestQuestionService;        // ★ 追加（サービス名も合わせてください）
import com.example.demo.service.WordService;

@Controller
public class HomeController {

    private final Job csvChunkJob;
    private final JobLauncher jobLauncher;
    private final UserService userService;
    private final WordService wordService;
    private final TestQuestionService testQuestionService;  // ★ 追加

    @Autowired
    public HomeController(
            @Qualifier("csvChunkJob") Job csvChunkJob,
            JobLauncher jobLauncher,
            UserService userService,
            WordService wordService,
            TestQuestionService testQuestionService) {       // ★ 追加
        this.csvChunkJob = csvChunkJob;
        this.jobLauncher = jobLauncher;
        this.userService = userService;
        this.wordService = wordService;
        this.testQuestionService = testQuestionService;     // ★ 追加
    }

 // ① userIdはStringで受けて安全に変換
    @GetMapping("/home")
    public String homePage(@RequestParam(name = "userId", required = false) String userIdParam, Model model) {
        Long userId = parseLongOrNull(userIdParam);
        UserEntity user = (userId != null) ? userService.getUserById(userId).orElse(null) : null;
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        return "home";
    }

    // ② import の userId も String で受ける
    @PostMapping("/import")
    public ResponseEntity<String> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "userId", required = false) String userIdParam) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("CSVファイルが空です");
        }

        Long userIdFromRequest = parseLongOrDefault(userIdParam, 0L);

        try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            // ヘッダー正規化（BOM除去・小文字・前後空白除去）
            Set<String> headers = csvParser.getHeaderMap().keySet().stream()
                    .map(HomeController::normalizeHeader)
                    .collect(Collectors.toSet());

            // 判定用セット
            Set<String> TEST_HEADERS = Set.of("sentence","audio","correct","options");

            boolean hasAllTestHeaders = TEST_HEADERS.stream().allMatch(headers::contains);
            boolean looksLikeWordCsv = headers.contains("word") || headers.contains("meaning")
                    || headers.contains("pictdescription") || headers.contains("part");

         // importCsv 内の判定直後に追加（log は任意のロガーでOK。ここでは System.out）
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
 // ===== TestQuestion 取込（内部変更なし、userIdはLongでもOK） =====
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


 // 置き換え（前回提案したもの）
    private static boolean hasSetterForUserId(TestQuestion q) {
        try {
            q.getClass().getMethod("setUserId", Long.class);
            return true;
        } catch (NoSuchMethodException e) {
            try {
                q.getClass().getMethod("setUserId", long.class);
                return true;
            } catch (NoSuchMethodException ignored) {
                return false;
            }
        }
    }


	// ===== Word 取込 =====
 // ===== Word 取込（get呼び出しをgetSafeに変更） =====
    private ResponseEntity<String> importWordCsv(CSVParser csvParser, Long userIdFromRequest) {
        long ok = 0, ng = 0;

        final Map<String, Integer> headerIndex = csvParser.getHeaderMap().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> normalizeHeader(e.getKey()),
                        Map.Entry::getValue));

        // ★ 追加: "word" ヘッダー必須チェック（無ければ即 400）
        if (!headerIndex.containsKey("word")) {
            return ResponseEntity.badRequest().body(
                "Word CSV と判定されましたが必須ヘッダー 'word' が見つかりません。\n" +
                "検出ヘッダー: " + headerIndex.keySet());
        }

        int line = 1;
        for (CSVRecord record : csvParser) {
            try {
                Word word = new Word();

                word.setWord(getSafe(record, headerIndex, "word"));
                word.setMeaning(getSafe(record, headerIndex, "meaning"));
                word.setPictDescription(getSafe(record, headerIndex, "pictDescription")); // 大文字混在も getSafe が吸収
                word.setPart(getSafe(record, headerIndex, "part"));

                String statusStr = getSafe(record, headerIndex, "status");
                boolean status = (statusStr != null) && "true".equalsIgnoreCase(statusStr.trim());
                word.setStatus(status);

                word.setUserId(userIdFromRequest != null ? userIdFromRequest : 0L);

                // "word" が null/空ならスキップ（保存側の NotNull 制約回避）
                if (word.getWord() == null || word.getWord().isBlank()) {
                    ng++;
                    System.err.println("（Word）行" + line + "をスキップ: 'word' が空です");
                } else {
                    wordService.saveWord(word);
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

 // ===== ユーティリティ（追加/改修） =====

 // ヘッダー用正規化：BOM除去 + 小文字 + trim
 private static String normalizeHeader(String h) {
     if (h == null) return null;
     String s = h.replace("\uFEFF", ""); // BOM除去
     return s.trim().toLowerCase(Locale.ROOT);
 }

 // String→Long 安全変換
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

 // ヘッダー名/インデックスの両方で安全に取得
//置き換え：getSafe は rec.get(keyLower) を一切直接呼ばない
private static String getSafe(CSVRecord rec, Map<String, Integer> headerIndex, String keyWanted) {
  if (rec == null || keyWanted == null) return null;

  // まず headerIndex から“正規化一致”するインデックスを探す
  String normWanted = normalizeHeader(keyWanted);
  Integer idx = headerIndex.get(normWanted);
  if (idx != null) {
      try {
          String v = rec.get(idx);
          return (v != null && !v.isBlank()) ? v.trim() : null;
      } catch (Exception ignore) {
          // 安全に null
          return null;
      }
  }

  // 次に CSVRecord の toMap() から“実キー”を見つける
  try {
      for (String realKey : rec.toMap().keySet()) {
          if (normalizeHeader(realKey).equals(normWanted)) {
              String v = rec.get(realKey); // 実キーでのみ取得
              return (v != null && !v.isBlank()) ? v.trim() : null;
          }
      }
  } catch (Exception ignore) {
      // 安全に null
  }

  // 見つからなければ null（呼び出し側で必須判定を行う）
  return null;
}


}
