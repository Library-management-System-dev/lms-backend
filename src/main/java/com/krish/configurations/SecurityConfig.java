package com.krish.configurations;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    private final JwtValidator jwtValidator;

    public SecurityConfig(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .sessionManagement(management->management.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                ))
                .authorizeHttpRequests(Authorize -> Authorize
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                        .requestMatchers("/api/subscription-plans").permitAll()
                        .requestMatchers("/api/book-review/book/**").permitAll()
                        .requestMatchers("/api/subscription-plans/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/books/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/books/**").hasRole("ADMIN")
                        .requestMatchers("/api/books/bulk").hasRole("ADMIN")
                        .requestMatchers("/api/genres/create").hasRole("ADMIN")
                        .requestMatchers("/api/genres/*/hard").hasRole("ADMIN")
                        .requestMatchers("/api/book-loans/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/book-loans/checkout/user/**").hasRole("ADMIN")
                        .requestMatchers("/api/fines").hasRole("ADMIN")
                        .requestMatchers("/api/fines/waive").hasRole("ADMIN")
                        .requestMatchers("/api/reservations/user/**").hasRole("ADMIN")
                        .requestMatchers("/api/reservations/*/fulfill").hasRole("ADMIN")
                        .requestMatchers("/api/subscription/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/payments").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtValidator, BasicAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                CorsConfiguration cfg = new CorsConfiguration();
                cfg.setAllowCredentials(true);
                cfg.setAllowedOrigins(
                        Arrays.asList(
                                "http://localhost:5173",
                                "https://krishlibrary.com"
                        )
                );
                cfg.setAllowedMethods(Collections.singletonList("*"));
                cfg.setAllowedHeaders(Collections.singletonList("*"));
                cfg.setExposedHeaders(Collections.singletonList("Authorization"));
                cfg.setMaxAge(3600L);
                return cfg;
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();

    }
}
