package com.example.demo.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.domain.entity.UserEntity;
import com.example.demo.domain.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component
public class UserInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (!userRepository.findByEmail("admin@example.com").isPresent()) {
            UserEntity user = new UserEntity();
            user.setEmail("admin@example.com");
            user.setUsername("admin");
            user.setPassword(passwordEncoder.encode("password"));
            user.setPremium(false);
            user.setCanWatchVideo(false);
            userRepository.save(user);
        }
    }
}
