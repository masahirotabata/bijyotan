package com.example.demo.api.controller;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;   // ★ 追加
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // ★ 追加

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
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder; // ★ 登録時に必ず使用

    public UserController(
        UserService userService,
        UserRepository userRepository,
        MailService mailService,
        PasswordEncoder passwordEncoder // ★ 追加
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder; // ★ 追加
    }

    // レベル更新
    @PutMapping("/updateLevel")
    public ResponseEntity<Map<String, Object>> updateLevel(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
        int level = Integer.parseInt(String.valueOf(payload.get("level")));

        Optional<UserEntity> opt = userRepository.findById(userId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        UserEntity user = opt.get();
        user.setLevel(level);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("level", user.getLevel()));
    }

    // ユーザー取得
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id)
            .map(user -> ResponseEntity.ok(
                new UserResponseDto(
                    user.getId(),
                    user.getUsername(),  // ★ 追加済み
                    user.isPremium(),
                    user.isCanWatchVideo(),
                    user.getLoginPoints(),
                    Optional.ofNullable(user.getLevel()).orElse(1),
                    Optional.ofNullable(user.getTestCorrectTotal()).orElse(0)
                )
            ))
            .orElse(ResponseEntity.notFound().build());
    }

    // 動画視聴フラグ ON
    @PutMapping("/{id}/setCanWatchVideo")
    public ResponseEntity<Void> setCanWatchVideo(@PathVariable("id") Long id) {
        if (userService.setCanWatchVideo(id, true)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    // 動画視聴フラグ OFF
    @PutMapping("/{id}/disableCanWatchVideo")
    public ResponseEntity<Void> disableAdVideo(@PathVariable("id") Long id) {
        if (userService.setCanWatchVideo(id, false)) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    // カジュアルスーツ解放
    @PutMapping("/{id}/unlockCasualSuit")
    public ResponseEntity<String> unlockCasualSuit(@PathVariable("id") Long id) {
        return userService.unlockCasualSuit(id);
    }

    // ユーザー登録（★ パスワードは必ず BCrypt で保存）
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
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password)); // ★ 平文保存禁止
        UserEntity savedUser = userRepository.save(user);

        // 登録後、静的ページへリダイレクト（APIはセッションで保護）
        URI redirectUri = URI.create("/user.html?userId=" + savedUser.getId());
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    // パスワードリセット開始（★ 現在ホストから動的生成）
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/forgot-password.html?error=notfound"))
                    .build();
        }

        UserEntity user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        String resetUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/reset-password.html")
                .queryParam("token", token)
                .build()
                .toUriString(); // ★ 本番/ローカルどちらでもOK

        mailService.sendResetPasswordMail(email, resetUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login.html?reset=success"))
                .build();
    }

    // プレミアム化
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
