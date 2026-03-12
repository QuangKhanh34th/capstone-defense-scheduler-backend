package com.capstone.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                // Vite development server
                                "http://localhost:5173",
                                "http://localhost:8080",
                                "http://localhost:50807", // Port hiện tại của anh
                                "http://localhost:5000",
                                "http://localhost:3000",
                                // Production frontend
                                "https://fptsystem.vercel.app/"
                        )
                        .allowedOriginPatterns(
                                "http://localhost:*",
                                "http://172.16.16.1:*"

                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}