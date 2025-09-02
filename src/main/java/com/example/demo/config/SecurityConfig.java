package com.example.demo.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // BCrypt（"Encoded password does not look like BCrypt" 対策）
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

  // 統合版 SecurityFilterChain
  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {

    http
        // 必要に応じて限定無効化へ変更OK（/auth/login, /user/register等だけ除外にするなど）
        .csrf(csrf -> csrf.disable())
        .headers(h -> h.frameOptions(f -> f.sameOrigin())) // H2 Console 用
        .authenticationProvider(provider)

        // 認可設定
        .authorizeHttpRequests(auth -> auth
            // 公開パス
            .requestMatchers(
                "/", "/*.html", "/favicon.*",
                "/login.html", "/register.html",
                "/forgot-password.html", "/reset-password.html",
                // 認証不要API
                "/login", "/signup",               // フォームログイン(POST /login)や互換ルート
                "/auth/login", "/auth/logout",     // JSONログインAPIを使う場合
                "/user/register", "/user/forgot-password",
                // ページ（HTML自体は閲覧可。中で呼ぶAPIは要認証）
                "/user.html", "/battle.html",
                // WebSocket / STOMP
                "/ws/**", "/topic/**", "/app/**",
                // 静的ファイル
                "/css/**", "/js/**", "/images/**", "/videos/**", "/se/**", "/static/**",
                // ライブラリ類
                "/webjars/**",
                // H2 Console（本番では外す）
                "/h2-console/**"
            ).permitAll()
            // それ以外は認証必須
            .anyRequest().authenticated()
        )

        // 未認証アクセス時の振る舞い：
        // - API(JSON/REST) には 401 を返す
        // - それ以外(HTML) は /login.html へリダイレクト
        .exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
          String accept = req.getHeader("Accept");
          String uri = req.getRequestURI();
          boolean wantsJson = accept != null && accept.contains("application/json");
          boolean apiLike = uri.startsWith("/api") || uri.startsWith("/user");
          if (wantsJson || apiLike) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          } else {
            res.sendRedirect("/login.html");
          }
        }))

        // フォームログイン（既存の login.html + name属性に合わせる）
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
        )

        // セッションを必要時に作成（JSESSIONID を発行）
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_NEEDED));

    return http.build();
  }
}
