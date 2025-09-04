package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .with(authorizationServer(), a -> a.oidc(Customizer.withDefaults()))
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults())
                .authorizeHttpRequests(a -> a.anyRequest().authenticated())
                .webAuthn(a -> a.rpName("bootiful").rpId("localhost").allowedOrigins("http://localhost:9090"))
                .oneTimeTokenLogin(configurer -> {
                    configurer.tokenGenerationSuccessHandler((_,
                                                              response,
                                                              oneTimeToken) -> {
                        response.getWriter().println("you've got console mail!");
                        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                        var mesg = "please go to http://localhost:9090/login/ott?token=" + oneTimeToken.getTokenValue();
                        System.out.println(mesg);
                    });
                })
                .build();
    }

    @Bean
    JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {
        var jdbc = new JdbcUserDetailsManager(dataSource);
        jdbc.setEnableUpdatePassword(true);
        return jdbc;
    }
}
