package com.example.demo.api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.domain.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    // Lombokを使わない明示的コンストラクタ
    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    /**
     * 既存ユーザーの平文パスワードをBCryptへ再エンコード
     * ($2a/$2b/$2y で始まらない文字列のみ対象)
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
