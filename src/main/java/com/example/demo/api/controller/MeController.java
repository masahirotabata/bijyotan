// src/main/java/com/example/demo/api/MeController.java
package com.example.demo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// ★ 追加
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.domain.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class MeController {

  @Autowired
  private UserRepository userRepository; // ★ 追加

  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication auth) {
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      return ResponseEntity.status(401).build();
    }
    Map<String, Object> body = new HashMap<>();
    body.put("name", auth.getName());
    body.put("authorities", auth.getAuthorities()
        .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    // ★ ここで userId を付与
    Long id = userRepository.findByEmail(auth.getName()).map(u -> u.getId()).orElse(null);
    body.put("id", id);
    return ResponseEntity.ok(body);
  }
}
