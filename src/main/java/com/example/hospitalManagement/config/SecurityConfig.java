package com.example.hospitalManagement.config;

import com.example.hospitalManagement.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JWTAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/api/auth/**",
                                "/index",
                                "/",
                                "/api/doctors/**"
                        ).permitAll()
                        .requestMatchers("/DashBoard/AdminDashboard", "/api/doctors/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")
                        .anyRequest().authenticated()

                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                
                return HashUtil.sha256(rawPassword.toString());
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                
                return HashUtil.sha256(rawPassword.toString()).equals(encodedPassword);
            }
        };
    }
}
