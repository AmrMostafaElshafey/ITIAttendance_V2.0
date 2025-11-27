package com.iti.attendance.config;

import com.iti.attendance.security.CustomAuthenticationFailureHandler;
import com.iti.attendance.security.CustomAuthenticationSuccessHandler;
import com.iti.attendance.security.EmployeeUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomAuthenticationFailureHandler failureHandler;
    private final EmployeeUserDetailsService userDetailsService;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                          CustomAuthenticationFailureHandler failureHandler,
                          EmployeeUserDetailsService userDetailsService) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/register", "/", "/index", "/admin/login", "/login", "/error", "/setup/**").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "HR_MANAGER", "HR_EMPLOYEE")
                        .requestMatchers("/manager/**").hasAnyRole("MANAGER", "BRANCH_MANAGER", "TRAINING_MANAGER")
                        .requestMatchers("/employee/**").hasAnyRole("EMPLOYEE", "MANAGER", "BRANCH_MANAGER", "TRAINING_MANAGER", "HR_MANAGER", "HR_EMPLOYEE", "ADMIN", "SECURITY")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout").permitAll())
                .httpBasic(Customizer.withDefaults());
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider;
        provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
