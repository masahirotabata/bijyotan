package com.example.demo.api.controller;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.dto.UserResponseDto;
import com.example.demo.api.service.UserService;
import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.service.MailService;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService; // ← ★これを追加



    public UserController(UserService userService, UserRepository userRepository, MailService mailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }
    
 // UserController.java
    @PutMapping("/updateLevel")
    public ResponseEntity<Map<String, Object>> updateLevel(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        int level = Integer.parseInt(payload.get("level").toString());

        Optional<UserEntity> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        UserEntity user = opt.get();
        user.setLevel(level);
        userRepository.save(user);

        // フロントがそのまま使えるように level を返す
        return ResponseEntity.ok(Map.of("level", user.getLevel()));
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(
                    new UserResponseDto(
                        user.getId(),
                        user.getUsername(),  // ← ★ これが抜けていた
                        user.isPremium(),
                        user.isCanWatchVideo(),
                        user.getLoginPoints(),
                        Optional.ofNullable(user.getLevel()).orElse(1),
                        Optional.ofNullable(user.getTestCorrectTotal()).orElse(0)// ← 追加
                    )
                ))
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}/setCanWatchVideo")
    public ResponseEntity<Void> setCanWatchVideo(@PathVariable("id") Long id) {
        if (userService.setCanWatchVideo(id, true)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/disableCanWatchVideo")
    public ResponseEntity<Void> disableAdVideo(@PathVariable("id") Long id) {
        if (userService.setCanWatchVideo(id, false)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{id}/unlockCasualSuit")
    public ResponseEntity<String> unlockCasualSuit(@PathVariable("id") Long id) {
        return userService.unlockCasualSuit(id);
    }
    
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
        @RequestParam("email") String email,
        @RequestParam("password") String password
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build(); // すでに存在
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(email);
        UserEntity savedUser = userRepository.save(user); // 保存してID取得

        // リダイレクト先を user.html?userId=〇〇 に設定
        URI redirectUri = URI.create("/user.html?userId=" + savedUser.getId());
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/forgot-password.html?error=notfound")).build();
        }

        UserEntity user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;
        mailService.sendResetPasswordMail(email, resetUrl);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/login.html?reset=success")).build();
    }
    
 // UserController.java
    @PutMapping("/upgrade")
    public ResponseEntity<?> upgradeToPremium(@RequestParam(name = "userId") Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }

        UserEntity user = optionalUser.get();

        if (Boolean.TRUE.equals(user.isPremium())) {
            return ResponseEntity.ok("すでにプレミアムユーザーです");
        }

        user.setPremium(true);
        userRepository.save(user);
        return ResponseEntity.ok("アップグレード成功");
    }

    
}
