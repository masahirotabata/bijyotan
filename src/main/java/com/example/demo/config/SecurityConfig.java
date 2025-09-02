package com.example.demo.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // ★ 既存
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // ★ 既存：BCrypt前提
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // ===== 共通Bean =====
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // ★ 既存の方針を維持（DBもBCrypt前提）
  }

  @Bean
  public DaoAuthenticationProvider authProvider(
      UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  // 任意：同一オリジン想定なら必須ではありません（別オリジン運用なら活かしてください）
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(
        "https://bijyotan.onrender.com",
        "http://localhost:8080",
        "http://127.0.0.1:8080"
    ));
    c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }

  // ===== Chain #1: API向け（/api/**） =====
  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http,
                                         DaoAuthenticationProvider provider) throws Exception {
    http
      .securityMatcher("/api/**") // ★ API専用チェーン
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // ★ APIはCSRF除外
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/auth/login", "/api/auth/register",
          "/api/battle/**" // ★ 既存許可を踏襲
        ).permitAll()
        .anyRequest().authenticated()
      )
      // ★ APIはリダイレクトさせない：200/401で応答
      .formLogin(form -> form
        .loginProcessingUrl("/api/auth/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .successHandler((req, res, auth) -> res.setStatus(200))
        .failureHandler((req, res, ex) -> res.sendError(401))
        .permitAll()
      )
      .logout(l -> l
        .logoutUrl("/api/auth/logout")
        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
      )
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)); // ★ セッション維持

    return http.build();
  }

  // ===== Chain #2: 既存UI向け（全体キャッチ） =====
  @Bean
  @Order(2)
  public SecurityFilterChain uiSecurity(HttpSecurity http,
                                        DaoAuthenticationProvider provider) throws Exception {
    http
      // .securityMatcher() を付けない＝残り全部
      .csrf(csrf -> csrf.enable()) // ★ UIはCSRF有効（フォーム運用）
      .headers(h -> h.frameOptions(f -> f.sameOrigin())) // ★ h2-console対策
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/", "/*.html", "/favicon.*",
          "/login.html", "/register.html",
          "/forgot-password.html", "/reset-password.html",
          "/user/register", "/user/forgot-password",
          "/login", "/signup",        // ★ 既存フォーム経路
          "/auth/**",                 // ★ 既存許可（必要なら残す）
          "/user.html",
          "/battle.html",
          "/ws/**", "/topic/**", "/app/**",
          "/css/**", "/js/**", "/images/**", "/videos/**", "/se/**", "/static/**",
          "/webjars/**",
          "/h2-console/**"
        ).permitAll()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login.html")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        .defaultSuccessUrl("/user.html", true)       // ★ 既存のリダイレクト維持
        .failureUrl("/login.html?error=true")
        .permitAll()
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login.html?logout")
        .permitAll()
      )
      // ★ ここを修正: IF_NEEDED -> IF_REQUIRED（またはこの行を削除でもOK）
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

    return http.build();
  }
}
