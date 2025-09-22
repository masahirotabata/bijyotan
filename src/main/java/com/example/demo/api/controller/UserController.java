package com.example.demo.api.controller;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserService userService,
            UserRepository userRepository,
            MailService mailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    /** レベル更新（戻り値の型を Map<String, Integer> に統一） */
    @PutMapping("/updateLevel")
    public ResponseEntity<Map<String, Integer>> updateLevel(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(String.valueOf(payload.get("userId")));
        int level = Integer.parseInt(String.valueOf(payload.get("level")));

        return userRepository.findById(userId)
                .map(u -> {
                    u.setLevel(level);
                    userRepository.save(u);
                    return ResponseEntity.ok(Map.of("level", u.getLevel()));
                })
                // notFound() のビルダー型を明示して型不一致を回避
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).<Map<String, Integer>>build());
    }

    /** ユーザー取得 */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(
                        new UserResponseDto(
                                user.getId(),
                                user.getUsername(),
                                // boolean アクセサに修正
                                user.isPremium(),
                                user.isCanWatchVideo(),
                                user.getLoginPoints(),
                                user.getLevel(),
                                Optional.ofNullable(user.getTestCorrectTotal()).orElse(0)
                        )
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    /** 動画視聴フラグ ON */
    @PutMapping("/{id}/setCanWatchVideo")
    public ResponseEntity<Void> setCanWatchVideo(@PathVariable("id") Long id) {
        return userService.setCanWatchVideo(id, true)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** 動画視聴フラグ OFF */
    @PutMapping("/{id}/disableCanWatchVideo")
    public ResponseEntity<Void> disableAdVideo(@PathVariable("id") Long id) {
        return userService.setCanWatchVideo(id, false)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    /** カジュアルスーツ解放 */
    @PutMapping("/{id}/unlockCasualSuit")
    public ResponseEntity<String> unlockCasualSuit(@PathVariable("id") Long id) {
        return userService.unlockCasualSuit(id);
    }

    /** ユーザー登録（パスワードは必ず BCrypt） */
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @RequestParam("email") String email,
            @RequestParam("password") String password
    ) {
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(email);
        user.setPassword(passwordEncoder.encode(password));

        // primitive なので null チェック不要。初期値を明示セット
        user.setLevel(1);
        user.setLoginPoints(0);
        user.setPremium(false);
        user.setCanWatchVideo(false);

        UserEntity saved = userRepository.save(user);
        URI redirectUri = URI.create("/user.html?userId=" + saved.getId());
        return ResponseEntity.status(HttpStatus.FOUND).location(redirectUri).build();
    }

    /** パスワードリセット開始（現在ホストから動的生成） */
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
                .toUriString();

        mailService.sendResetPasswordMail(email, resetUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/login.html?reset=success"))
                .build();
    }

     /** プレミアム化 */
     // 例: com.example.demo.api.controller.UserController
    
    @PutMapping("/upgrade")
    public ResponseEntity<String> upgradeToPremium(@RequestParam("userId") Long userId) {
        Optional<UserEntity> opt = userRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("ユーザーが見つかりません");
        }
        UserEntity user = opt.get();
        if (!user.isPremium()) {
            user.setPremium(true);
            userRepository.save(user);
        }
        return ResponseEntity.ok("アップグレード成功");
    }

    /** 画面遷移用（GET）。内部で同じ処理を実行し、その後 user.html に 302 リダイレクト */
    @GetMapping("/upgrade-redirect")
    public ResponseEntity<Void> upgradeAndRedirect(@RequestParam("userId") Long userId) {
        // 上の処理を再利用してもOK。重複が気になるなら共通privateメソッド化してください。
        Optional<UserEntity> opt = userRepository.findById(userId);
        if (opt.isPresent() && !opt.get().isPremium()) {
            UserEntity user = opt.get();
            user.setPremium(true);
            userRepository.save(user);
        }
        URI to = URI.create("/user.html?userId=" + userId);
        return ResponseEntity.status(HttpStatus.FOUND).location(to).build(); // 302
    }
}
