package com.example.demo.config;

import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.domain.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  /* ========== Common Beans ========== */

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  // 別オリジンから叩く可能性がある場合のみ必要（同一オリジン運用でも害はありません）
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(
        "https://bijyotan.onrender.com",
        "https://liferabbit-todo-web.onrender.com", 
        "http://localhost:8080",
        "http://127.0.0.1:8080"
    ));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
    s.registerCorsConfiguration("/**", c);
    return s;
  }

  /* ========== Chain #1: API (/api/**) ========== */

  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
    http
      .securityMatcher("/api/**")
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))      // APIはCSRF対象外（JSON/AJAX想定）
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/battle/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/words", "/api/words/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/test-questions", "/api/test-questions/**").permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
      )
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
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

    return http.build();
  }

  /* ========== Chain #2: UI (その他) ========== */

  @Bean
  @Order(2)
  public SecurityFilterChain uiSecurity(
      HttpSecurity http,
      DaoAuthenticationProvider provider,
      UserRepository userRepository) throws Exception {

    http
      // ★ CSRF除外は一回に集約：フォーム系とプレミアム更新を除外
      .csrf(csrf -> csrf.ignoringRequestMatchers(
          "/login",
          "/user/register",
          "/user/forgot-password",
          "/upgrade",
          "/user/upgrade" // ← premium アップグレード用
      ))
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/", "/*.html", "/favicon.*",
          "/login.html", "/register.html",
          "/forgot-password.html", "/reset-password.html",
          "/user/register", "/user/forgot-password",
          "/login", "/signup",
          "/auth/**",
          "/user.html",
          "/battle.html",
          "/register.html",
          "/ws/**", "/topic/**", "/app/**",
          "/css/**", "/js/**", "/images/**", "/videos/**", "/se/**", "/static/**",
          "/webjars/**",
          "/h2-console/**"
        ).permitAll()
        // ★ 認証不要でPUT /upgradeを許可（ログイン必須にしたい場合はここを authenticated に変更）
        .requestMatchers(HttpMethod.PUT, "/upgrade", "/user/upgrade").permitAll()
        .requestMatchers(HttpMethod.GET, "/user/upgrade-redirect").permitAll() // ← 追加
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login.html")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
        // ログイン成功後：LoginControllerのloginSuccessで処理→/user.html?userId=... へ
        .successHandler((req, res, auth) -> res.sendRedirect("/loginSuccess"))
        .failureUrl("/login.html?error=true")
        .permitAll()
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login.html?logout")
        .permitAll()
      )
      .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

    return http.build();
  }
}
