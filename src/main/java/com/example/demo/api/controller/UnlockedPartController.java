package com.example.demo.api.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.entity.UnlockedPart;
import com.example.demo.domain.repository.UnlockedPartRepository;

@RestController
@RequestMapping("/api/unlocked")
public class UnlockedPartController {

    private final UnlockedPartRepository unlockedPartRepository;

    @Autowired
    public UnlockedPartController(UnlockedPartRepository unlockedPartRepository) {
        this.unlockedPartRepository = unlockedPartRepository;
    }

    @PostMapping("/unlock")
    public ResponseEntity<String> unlockPart(
            @RequestParam Long userId,
            @RequestParam String part) {

        if (!unlockedPartRepository.existsByUserIdAndPart(userId, part)) {
            UnlockedPart unlocked = new UnlockedPart();
            unlocked.setUserId(userId);
            unlocked.setPart(part);
            unlocked.setUnlockedAt(LocalDateTime.now());
            unlockedPartRepository.save(unlocked);
        }

        return ResponseEntity.ok("パート解放済み");
    }
}
