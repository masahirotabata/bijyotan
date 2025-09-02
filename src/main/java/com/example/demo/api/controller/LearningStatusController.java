package com.example.demo.api.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.repository.LearningStatusRepository;
import com.example.demo.service.LearningStatusService;

@RestController
@RequestMapping("/api/learning")
public class LearningStatusController {

    @Autowired
    private LearningStatusService learningStatusService;
  
    @Autowired
    private LearningStatusRepository learningStatusRepository;

    @PutMapping("/update")
    public ResponseEntity<?> updateLearningStatus(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            Long wordId = Long.valueOf(body.get("wordId").toString());
            boolean learned = Boolean.parseBoolean(body.get("status").toString());
            String part = body.get("part").toString();

            learningStatusService.updateStatus(userId, wordId, part, learned);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("更新失敗");
        }
    }

 // ✅ 修正済み：@RequestParam("userId") と @RequestParam("part")
    @GetMapping("/count")
    public ResponseEntity<?> getLearningStatusCount(@RequestParam("userId") Long userId,
                                                    @RequestParam("part") String part) {
        return ResponseEntity.ok(learningStatusService.getLearningStatusCount(userId, part));
    }


    // ヘルパーメソッド
    private int convertPartToInt(String part) {
        if (part == null) return 0;
        part = part.trim().toLowerCase();
        if (part.startsWith("part")) {
            try {
                return Integer.parseInt(part.substring(4));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}
