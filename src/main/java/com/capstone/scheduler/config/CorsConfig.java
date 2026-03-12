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
<<<<<<< HEAD
                                "http://localhost:51426"
=======
                                "http://localhost:51426",

                                // Production frontend (Both with and without trailing slash)
                                "https://fptsystem.vercel.app",
                                "https://fptsystem.vercel.app/"
>>>>>>> fa77303ad92051be95411f27bf09d2ecaa5e8166
                        )
                        .allowedOriginPatterns(
                                "http://localhost:*"
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