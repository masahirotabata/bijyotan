package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // ★ 追加
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authProvider(
      UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider p = new DaoAuthenticationProvider();
    p.setUserDetailsService(uds);
    p.setPasswordEncoder(encoder);
    return p;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {

    http
      .csrf(csrf -> csrf.disable())
      .headers(h -> h.frameOptions(f -> f.sameOrigin()))
      .authenticationProvider(provider)
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/", "/*.html", "/favicon.*",
          "/login.html", "/register.html",
          "/forgot-password.html", "/reset-password.html",
          "/user/register", "/user/forgot-password",
          "/login", "/signup",
           "/auth/**",                 // ★ 追加: APIログインを許可
          "/user.html",
          "/battle.html", "/api/battle/**",
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
        .defaultSuccessUrl("/user.html", true)
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
