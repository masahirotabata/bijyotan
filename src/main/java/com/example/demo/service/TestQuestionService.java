package com.example.demo.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.domain.entity.TestQuestion;

public interface TestQuestionService {
  TestQuestion save(TestQuestion q);
  List<TestQuestion> saveAll(List<TestQuestion> list);

  /** CSV を取り込んで保存し、結果メッセージを返す */
  String importCsv(MultipartFile file, Long fixedUserId) throws Exception;
}
