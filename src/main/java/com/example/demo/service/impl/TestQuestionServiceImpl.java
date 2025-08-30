package com.example.demo.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.domain.entity.TestQuestion;
import com.example.demo.domain.repository.TestQuestionRepository;
import com.example.demo.service.TestQuestionService;

@Service
@Transactional
public class TestQuestionServiceImpl implements TestQuestionService {

  private final TestQuestionRepository repository;

  public TestQuestionServiceImpl(TestQuestionRepository repository) {
    this.repository = repository;
  }

  @Override
  public TestQuestion save(TestQuestion q) {
    if (q == null) throw new IllegalArgumentException("TestQuestion must not be null");
    return repository.save(q);
  }

  @Override
  public List<TestQuestion> saveAll(List<TestQuestion> list) {
    return repository.saveAll(list);
  }

  // ---------- CSV 取り込み ----------
  @Override
  public String importCsv(MultipartFile file, Long fixedUserId) throws Exception {
    long ok = 0, ng = 0;

    try (var reader = new BufferedReader(
           new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
         var parser = new CSVParser(reader, CSVFormat.DEFAULT
             .withFirstRecordAsHeader()
             .withIgnoreHeaderCase()
             .withTrim())) {

      final Map<String,Integer> header = parser.getHeaderMap().entrySet().stream()
          .collect(Collectors.toMap(e -> norm(e.getKey()), Map.Entry::getValue));

      for (CSVRecord r : parser) {
        try {
          TestQuestion q = new TestQuestion();
          q.setSentence(get(r, header, "sentence"));
          q.setOptions(get(r, header, "options"));
          q.setCorrect(get(r, header, "correct"));
          q.setAudio(get(r, header, "audio"));

          Long csvUserId = parseLongOrNull(get(r, header, "userId"));
          q.setUserId(csvUserId != null ? csvUserId : fixedUserId);

          if (isBlank(q.getSentence()) || isBlank(q.getCorrect())) { ng++; continue; }

          repository.save(q);
          ok++;
        } catch (Exception ex) {
          ng++;
        }
      }
    }
    return "TestQuestion CSVアップロード結果: OK=" + ok + ", NG=" + ng;
  }

  // ---------- helpers ----------
  private static String norm(String s){
    if (s == null) return null;
    return s.replace("\uFEFF","").trim().toLowerCase(Locale.ROOT);
  }
  private static String get(CSVRecord rec, Map<String,Integer> header, String wanted){
    Integer idx = header.get(norm(wanted));
    if (idx == null) return null;
    String v = rec.get(idx);
    return (v == null || v.isBlank()) ? null : v.trim();
  }
  private static boolean isBlank(String s){ return s == null || s.isBlank(); }
  private static Long parseLongOrNull(String s){
    try { return (s==null||s.isBlank()) ? null : Long.valueOf(s.trim()); }
    catch(Exception e){ return null; }
  }
}
