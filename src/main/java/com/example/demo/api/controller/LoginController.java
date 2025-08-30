package com.example.demo.api.controller;

import java.security.Principal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private final UserRepository userRepository;

    @Autowired
    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ ログイン成功後に呼ばれる（ログインボーナス処理含む）
    @GetMapping("/loginSuccess")
    public String loginSuccess(HttpSession session, Model model, Principal principal) {
        UserEntity user = userRepository.findByEmail(principal.getName()).orElse(null);
        if (user == null) return "redirect:/login.html";

        // ✅ ログインボーナス処理（1日1回）
        LocalDate today = LocalDate.now();
        if (user.getLastLoginDate() == null || !user.getLastLoginDate().isEqual(today)) {
            user.setLoginPoints(user.getLoginPoints() + 1);
            user.setLastLoginDate(today);
            if (user.getLoginPoints() >= 30) {
                user.setCasualsuitUnlocked(true); // 30日連続ログインで解放
            }
            userRepository.save(user);
        }

        return "redirect:/user.html?userId=" + user.getId();
    }
}
