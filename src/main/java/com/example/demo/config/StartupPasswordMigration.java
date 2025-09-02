// src/main/java/com/example/demo/config/StartupPasswordMigration.java
package com.example.demo.config;

import com.example.demo.api.service.AuthService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupPasswordMigration {

    @Bean
    public ApplicationRunner passwordMigration(AuthService authService) {
        return args -> authService.reencodePlainPasswords();
    }
}
