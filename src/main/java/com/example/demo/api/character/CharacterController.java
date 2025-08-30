package com.example.demo.api.character;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.character.dto.CharacterDto;
import com.example.demo.service.CharacterRegistry;
import com.example.demo.service.SelectedCharacterStore;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
class CharacterController {
  private final CharacterRegistry registry;
  private final SelectedCharacterStore store; // ユーザーの選択保持（メモリでも可）

  @GetMapping public List<CharacterDto> list(){ return registry.all(); }

  @PostMapping("/select")
  public void select(@AuthenticationPrincipal(expression="id") Long userId,
                     @RequestBody Map<String,Long> body){
    store.set(userId, body.get("characterId"));
  }
}