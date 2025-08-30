package com.example.demo.domain.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USERS")
@Data
@NoArgsConstructor
public class UserEntity {

    private boolean casualsuitUnlocked;
    private boolean premium;
    private LocalDate lastLoginDate;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "is_premium", nullable = false)
    private boolean isPremium = false;

    @Column(name = "can_watch_video", nullable = false)
    private boolean canWatchVideo = false;

    @Column(name = "login_points", nullable = false)
    private int loginPoints = 0;

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "test_correct_total")
    private int testCorrectTotal = 0;

    @Column(name = "test_correct") // ✅ 追加
    private int testCorrect = 0;

    @Column(name = "test_total") // ✅ 追加
    private int testTotal = 0;

    @Column(name = "level")
    private int level = 0;

    @Column(name = "frilly_unlocked")
    private boolean frillyUnlocked = false;

    @Column(name = "hotpants_unlocked")
    private boolean hotpantsUnlocked = false;

    // Getter & Setter（必要なものだけ書き出し）

    public int getTestCorrect() {
        return testCorrect;
    }

    public void setTestCorrect(int testCorrect) {
        this.testCorrect = testCorrect;
    }

    public int getTestTotal() {
        return testTotal;
    }

    public void setTestTotal(int testTotal) {
        this.testTotal = testTotal;
    }

    public boolean isCasualsuitUnlocked() {
        return casualsuitUnlocked;
    }

    public void setCasualsuitUnlocked(boolean casualsuitUnlocked) {
        this.casualsuitUnlocked = casualsuitUnlocked;
    }

    public LocalDate getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDate lastLogin) {
        this.lastLogin = lastLogin;
    }

    // その他 Lombok の @Data で自動生成
}
