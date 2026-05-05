package com.demo.EmployeeDb.config;

import com.demo.EmployeeDb.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.demo.EmployeeDb.security.JwtFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // NEW — handles 401 and 403 directly from Spring Security
            .exceptionHandling(ex -> ex

                // 401 — No token or invalid token reaching Spring Security
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    ApiResponse apiResponse = new ApiResponse(
                        401,
                        "Unauthorized: You must login first to access this resource.",
                        null
                    );
                    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                })

                // 403 — Authenticated but wrong role
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    ApiResponse apiResponse = new ApiResponse(
                        403,
                        "Access Denied: You do not have permission to perform this action.",
                        null
                    );
                    response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                })
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/configuration/**"
                    
                // Permission for all HTTP methods
                ).permitAll()
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/employee").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/employee/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/employee/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/employee/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_USER")
                .requestMatchers(HttpMethod.DELETE, "/employee/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/department/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/department/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/department/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/department/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}