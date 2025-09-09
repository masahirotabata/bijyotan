// AuthController.java
package com.example.demo.api.controller;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserDetailsService userDetailsService; // 自動ログインに使用（任意）

  public AuthController(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        UserDetailsService userDetailsService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userDetailsService = userDetailsService;
  }

  public static class RegisterRequest {
    public String name;
    public String email;
    public String password;
  }

  @PostMapping(
      path = "/register",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest req,
                                                      HttpServletRequest request) {
    if (req.email == null || req.password == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "email/password required"));
    }
    if (userRepository.findByEmail(req.email).isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "email already used"));
    }

    UserEntity user = new UserEntity();
    user.setEmail(req.email);
    user.setUsername(req.name != null && !req.name.isBlank() ? req.name : req.email);
    user.setPassword(passwordEncoder.encode(req.password));
    user.setLevel(1);
    user.setLoginPoints(0);
    user.setPremium(false);
    user.setCanWatchVideo(false);

    UserEntity saved = userRepository.save(user);

    // ---- 任意：登録直後にログイン状態にする ----
    var ud = userDetailsService.loadUserByUsername(saved.getEmail());
    var auth = UsernamePasswordAuthenticationToken.authenticated(ud, null, ud.getAuthorities());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    request.getSession(true); // セッション確保

    URI location = URI.create("/user/" + saved.getId());
    return ResponseEntity
        .created(location) // 201 Created + Location
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("id", saved.getId(), "userId", saved.getId()));
  }
}
