package com.example.demo.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.demo.domain.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class MeController {

  private final UserRepository userRepository;

  public MeController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/me")
  public Map<String, Object> me(Authentication auth) {
    // 未認証なら 401
    if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    Long id = userRepository.findByEmail(auth.getName())
        .map(u -> u.getId())        // UserEntity::getId の代わり（import不要）
        .orElse(null);

    Map<String, Object> body = new HashMap<>();
    body.put("id", id);
    body.put("name", auth.getName());
    body.put("authorities",
        auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
    return body;
  }
}
