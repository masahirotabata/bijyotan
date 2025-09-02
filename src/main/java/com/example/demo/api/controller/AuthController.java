// src/main/java/com/example/demo/api/controller/AuthController.java
package com.example.demo.api.controller;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request) {

        UserEntity u = users.findByEmail(email).orElse(null);
        if (u == null || u.getPassword() == null || !encoder.matches(password, u.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("message", "メールまたはパスワードが違います"));
        }

        var principal = User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles("USER").build();
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true); // セッション発行

        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        request.getSession(false); // 既存セッションがあれば
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
