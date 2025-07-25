package com.example.projectmanagement.config;
 
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.web.SecurityFilterChain;
 
@Configuration

@EnableWebSecurity

public class SecurityConfig {
 
    // Placeholder JWKS endpoint — update this later

    private static final String JWKS_URI = "https://dummy-domain.com/.well-known/jwks.json";
 
    @Bean

    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/auth/**").permitAll()

                .anyRequest().authenticated()

            )

            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));
 
        return http.build();

    }
 
    @Bean

    public JwtDecoder jwtDecoder() {

        return NimbusJwtDecoder.withJwkSetUri(JWKS_URI).build();

    }

}

 