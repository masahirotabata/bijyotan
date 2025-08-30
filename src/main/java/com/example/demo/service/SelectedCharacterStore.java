package com.example.demo.service;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SelectedCharacterStore {

  private final ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<>();

  public void set(Long userId, Long characterId) {
    if (userId != null && characterId != null) map.put(userId, characterId);
  }

  public Long get(Long userId) {
    if (userId == null) return null;
    return map.get(userId);
  }
}
