package com.example.demo.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    Map<String, Object> body = new HashMap<>();
    body.put("name", auth.getName()); // = email にしているはず
    body.put("authorities", auth.getAuthorities()
        .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
    return ResponseEntity.ok(body);
  }
}
