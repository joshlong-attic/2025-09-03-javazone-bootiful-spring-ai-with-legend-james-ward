package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}

@Configuration
class GatewayConfig {


    @Order(0)
    @Bean
    RouterFunction<ServerResponse> httpApiRoute() {
        return route()
                .before(BeforeFilterFunctions.rewritePath("/api/*", "/"))
                .filter(FilterFunctions.uri("http://localhost:8080"))
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .GET("/api/**", http())
                .build();
    }

    @Order(1)
    @Bean
    RouterFunction<ServerResponse> uiRoute() {
        return route()
                .filter(FilterFunctions.uri("http://localhost:8020"))
                .GET("/**", http())
                .build();
    }

}