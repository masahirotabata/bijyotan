package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // WebSocket/SockJS を使うなら CSRF は全体無効でOK（必要なら限定無効にしても良い）
      .csrf(csrf -> csrf.disable())
      .headers(h -> h.frameOptions(frame -> frame.sameOrigin())) // H2 Console 用

      .authorizeHttpRequests(auth -> auth
        // 1) まずは許可パスを全部列挙
        .requestMatchers(
          "/",
          "/login.html", "/register.html",
          "/forgot-password.html", "/reset-password.html",
          "/user/register", "/user/forgot-password",
          "/user.html",
          "/battle.html", "/api/battle/**",
          // STOMP/SockJS (handshake や topic などは匿名でも購読できるようにする想定)
          "/ws/**", "/topic/**", "/app/**",
          // 静的ファイル
          "/css/**", "/js/**", "/images/**", "/videos/**", "/se/**", "/static/**",
          // H2 Console
          "/h2-console/**",
          "/images/**", "/videos/**", "/css/**", "/js/**",
          "/webjars/**", "/*.html", "/favicon.*"
        ).permitAll()

        // 2) 最後に other を認証ありに
        .anyRequest().authenticated()
      )

      // フォームログインは一度だけ設定
      .formLogin(form -> form
        .loginPage("/login.html")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .defaultSuccessUrl("/user.html", true)
        .failureUrl("/login.html?error=true")
        .permitAll()
      )
      .logout(logout -> logout.permitAll());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
  }
}
