package com.example.demo.config;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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
import org.springframework.http.HttpMethod;

import com.example.demo.domain.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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

  // 別オリジンから叩く想定がある場合のみ有効に（同一オリジンのみなら不要）
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of(
        "https://bijyotan.onrender.com",
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

  // ===== Chain #1: API向け (/api/**) =====
  @Bean
  @Order(1)
  public SecurityFilterChain apiSecurity(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
    http
      .securityMatcher("/api/**")
      .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**")) // APIはCSRF対象外（fetch/JSON 用）
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        // ★ JSON 登録APIを許可
        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/battle/**").permitAll()
        // ★ 読み取り系だけ許可（必要に応じて調整）
        .requestMatchers(HttpMethod.GET, "/api/words", "/api/words/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/test-questions", "/api/test-questions/**").permitAll()
        .anyRequest().authenticated()
      )
      .exceptionHandling(ex -> ex
        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
      )
      // APIはAJAX前提: 成功=200 / 失敗=401
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

  // ===== Chain #2: UI向け（それ以外） =====
  @Bean
  @Order(2)
  public SecurityFilterChain uiSecurity(
      HttpSecurity http,
      DaoAuthenticationProvider provider,
      UserRepository userRepository) throws Exception {

    http
      // ★ premium アップグレード用エンドポイントを CSRF 対象外にする
      .csrf(csrf -> csrf.ignoringRequestMatchers(
          "/login", "/user/register", "/user/forgot-password",
          "/upgrade"                    // ← 追加
      ))
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))
      .cors(Customizer.withDefaults())
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        // 静的/画面系は公開
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
        // ★ PUT /upgrade を許可（ログイン不要で動かしたい場合）
        .requestMatchers(HttpMethod.PUT, "/upgrade").permitAll()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login.html")
        .loginProcessingUrl("/login")
        .usernameParameter("email")
        .passwordParameter("password")
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
