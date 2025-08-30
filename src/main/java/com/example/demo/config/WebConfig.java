package com.example.demo.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("http://localhost:8080", "http://127.0.0.1:8080") // 両方OKにする
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
        	Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            registry.addResourceHandler("/uploads/**")
                     .addResourceLocations("file:" + uploadDir.toString() + "/");
            registry.addResourceHandler("/images/**")
                    .addResourceLocations("file:./src/main/resources/static/images/");
            registry.addResourceHandler("/videos/**")
                    .addResourceLocations("file:./src/main/resources/static/videos/");
        }
    }
