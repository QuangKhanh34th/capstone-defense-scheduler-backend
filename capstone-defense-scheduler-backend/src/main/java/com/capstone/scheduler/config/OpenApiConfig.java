package com.capstone.scheduler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Defense Scheduler API")
                        .version("1.0")
                        .description("Tài liệu API cho hệ thống xếp lịch bảo vệ đồ án"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Server Local")
                ));
    }
}