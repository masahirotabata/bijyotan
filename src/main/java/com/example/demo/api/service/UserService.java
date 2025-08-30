package com.example.demo.api.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean setCanWatchVideo(Long id, boolean value) {
        return userRepository.findById(id).map(user -> {
            user.setCanWatchVideo(value);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public ResponseEntity<String> unlockCasualSuit(Long id) {
        return userRepository.findById(id)
            .map(user -> {
                user.setCasualsuitUnlocked(true);
                userRepository.save(user);
                return ResponseEntity.ok("スーツ解放成功");
            })
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません"));
    }
    
 // ログイン時に呼び出される処理
    public void applyLoginBonus(UserEntity user) {
        LocalDate today = LocalDate.now();
        if (user.getLastLogin() == null || !user.getLastLogin().isEqual(today)) {
            user.setLoginPoints(user.getLoginPoints() + 1);
            user.setLastLogin(today);
            userRepository.save(user);
        }
    }
}
