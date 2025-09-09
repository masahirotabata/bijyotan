// src/main/java/com/example/demo/api/controller/MeController.java
package com.example.demo.api.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class MeController {

  private final UserRepository userRepository;

  public MeController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication auth) {
    // 未認証なら 401
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    UserEntity u = userRepository.findByEmail(auth.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

    // フロントの normalize() が読むキーをすべて用意
    Map<String, Object> body = new HashMap<>();
    body.put("id", u.getId());
    body.put("username", Optional.ofNullable(u.getUsername()).orElse(u.getEmail())); // ← 表示名
    body.put("premium", u.isPremium());
    body.put("canWatchVideo", u.isCanWatchVideo());
    body.put("loginPoints", u.getLoginPoints());
    body.put("level", u.getLevel());
    body.put("testCorrectTotal", Optional.ofNullable(u.getTestCorrectTotal()).orElse(0));

    // 互換用のおまけ情報（あっても害なし）
    body.put("name", auth.getName());
    body.put("authorities", auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList()));

    return body;
  }
}
