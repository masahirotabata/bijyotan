package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // BCrypt（ログに出ていた「Encoded password does not look like BCrypt」対策）
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // UserDetailsService と PasswordEncoder をひも付け
  @Bean
  public DaoAuthenticationProvider authProvider(
      UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  // 統合版 SecurityFilterChain（これを1つだけ定義してください）
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {

    http
      .csrf(csrf -> csrf.disable())                          // 必要なら限定的に無効化へ調整
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))     // H2 Console 用

      .authenticationProvider(provider)

      .authorizeHttpRequests(auth -> auth
        // 公開パス
        .requestMatchers(
          "/", "/*.html", "/favicon.*",
          "/login.html", "/register.html",
          "/forgot-password.html", "/reset-password.html",
          "/user/register", "/user/forgot-password",
          "/login", "/signup",                 // 旧ルートも許可
          "/user.html",                        // HTML自体は閲覧可（APIは要認証）
          "/battle.html", "/api/battle/**",
          // WebSocket / STOMP
          "/ws/**", "/topic/**", "/app/**",
          // 静的ファイル
          "/css/**", "/js/**", "/images/**", "/videos/**", "/se/**", "/static/**",
          // ライブラリ類
          "/webjars/**",
          // H2 Console（本番では外すのが安全）
          "/h2-console/**"
        ).permitAll()
        // それ以外は認証必須
        .anyRequest().authenticated()
      )

      // フォームログイン設定（loginProcessingUrl と name 属性に合わせる）
      .formLogin(form -> form
        .loginPage("/login.html")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .defaultSuccessUrl("/user.html", true)
        .failureUrl("/login.html?error=true")
        .permitAll()
      )

      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login.html?logout")
        .permitAll()
      );

    return http.build();
  }
}
