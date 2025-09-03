package com.example.demo.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MeController {

  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication auth) {
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(401).build();
    }
    List<String> authorities = auth.getAuthorities()
        .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

    return ResponseEntity.ok(Map.of(
        "name", auth.getName(),   // email を返す想定
        "authorities", authorities
    ));
  }
}
