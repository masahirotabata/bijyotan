package com.example.demo.api.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // ★ 追加
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;       // ★ 追加

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // ★ 追加

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder; // ★ 追加
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public boolean setCanWatchVideo(Long id, boolean value) {
        return userRepository.findById(id).map(user -> {
            user.setCanWatchVideo(value);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Transactional
    public ResponseEntity<String> unlockCasualSuit(Long id) {
        return userRepository.findById(id)
            .map(user -> {
                user.setCasualsuitUnlocked(true);
                userRepository.save(user);
                return ResponseEntity.ok("スーツ解放成功");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません"));
    }

    // ★ 既存ユーザーの平文パスワードをBCryptへ一括再ハッシュ（$2a/$2b/$2y以外を対象）
    @Transactional
    public void reencodePlainPasswords() {
        userRepository.findAll().forEach(u -> {
            String p = u.getPassword();
            if (p != null && !p.startsWith("$2")) {
                u.setPassword(passwordEncoder.encode(p));
                userRepository.save(u);
            }
        });
    }

    // ログイン時ボーナス（★ null安全）
    @Transactional
    public void applyLoginBonus(UserEntity user) {
        LocalDate today = LocalDate.now();
        if (user.getLastLogin() == null || !today.equals(user.getLastLogin())) {
            int current = Optional.ofNullable(user.getLoginPoints()).orElse(0);
            user.setLoginPoints(current + 1);
            user.setLastLogin(today);
            userRepository.save(user);
        }
    }
}
