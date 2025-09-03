// src/main/java/com/example/demo/api/service/AuthService.java
package com.example.demo.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.domain.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    // 明示的コンストラクタ（Lombok不要）
    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /**
     * 既存ユーザーの平文パスワードを BCrypt へ再エンコード
     * （"$2" で始まらないものだけ対象）
     */
    @Transactional
    public void reencodePlainPasswords() {
        users.findAll().forEach(u -> {
            String p = u.getPassword();
            if (p != null && !p.startsWith("$2")) {
                u.setPassword(encoder.encode(p));
                users.save(u);
            }
        });
    }
}
