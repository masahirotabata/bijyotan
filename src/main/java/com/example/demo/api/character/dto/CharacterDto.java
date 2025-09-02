package com.example.demo.api.character.dto;

import java.util.List;

// キャラの定義。skills を追加（Registry で使っているため）
public record CharacterDto(
    long id,
    String slug,
    String name,
    int unlockLevel,
    String portraitUrl,
    String cutinUrl,
    String voiceDir,
    String color,
    List<String> skills   // 例: ["lost","eternal"]
) {}
