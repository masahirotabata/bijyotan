package com.example.demo.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Array;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SentenceQuizController {

    private final JdbcTemplate jdbc;

    @GetMapping("/sentence-quiz")
    public List<Map<String, Object>> list(
            @RequestParam(defaultValue = "adverbJA") String part,
            @RequestParam(defaultValue = "20") int limit
    ) {
        // 1〜1000の範囲にクランプ
        final int safeLimit = Math.min(Math.max(limit, 1), 1000);

        // SentenceQuizController の SQL を差し替え
        final String sql = """
          SELECT q.id, q.sentence, q.audio, q.correct, q.options, q.answer_key,
                 w.meaning AS ja     -- ★追加
          FROM sentence_quiz q
          LEFT JOIN word w ON w.word = q.answer_key
          WHERE q.part = ?
          ORDER BY random()
          LIMIT ?
        """;


        return jdbc.query(sql, (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", rs.getLong("id"));
            m.put("sentence", rs.getString("sentence"));
            m.put("audio", rs.getString("audio"));
            m.put("correct", rs.getString("correct"));

            // text[] → List<String>
            Array sqlArr = rs.getArray("options");
            List<String> opts = Collections.emptyList();
            if (sqlArr != null) {
                Object raw = sqlArr.getArray();
                if (raw instanceof String[]) {
                    opts = Arrays.asList((String[]) raw);
                } else if (raw instanceof Object[]) {
                    Object[] oa = (Object[]) raw;
                    List<String> tmp = new ArrayList<>(oa.length);
                    for (Object o : oa) tmp.add(String.valueOf(o));
                    opts = tmp;
                }
            }
            m.put("options", opts);

            m.put("answerKey", rs.getString("answer_key"));
            m.put("ja", rs.getString("ja"));  // ★追加
            return m;
        }, part, safeLimit);
    }
}
