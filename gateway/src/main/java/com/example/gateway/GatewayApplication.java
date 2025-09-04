package com.example.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.TokenRelayFilterFunctions;
import org.springframework.context.annotation.Bean;
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


    @Bean
    @Order(1)
    RouterFunction<ServerResponse> uiRoute() {
        return route()
                .filter(FilterFunctions.uri("http://localhost:8020"))
                .GET("/**", http())
                .build();
    }

    @Order(0)
    @Bean
    RouterFunction<ServerResponse> assistantRoute() {
        return route()
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))
                .filter(FilterFunctions.rewritePath("/assistant/*", "/"))
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .GET("/assistant/**", http())
                .build();
    }



    @Order(0)
    @Bean
    RouterFunction<ServerResponse> dogsRoute() {
        return route()
                .before(BeforeFilterFunctions.uri("http://localhost:8080"))
                .filter(FilterFunctions.rewritePath("/dogs/*", "/"))
                .filter(TokenRelayFilterFunctions.tokenRelay())
                .GET("/dogs/**", http())
                .build();
    }


}
