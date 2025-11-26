package com.iti.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // نسمح لجميع الطلبات ونترك التحكم في تسجيل الدخول للطبقة المخصصة داخل التطبيق
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
