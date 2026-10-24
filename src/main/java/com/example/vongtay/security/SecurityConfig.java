package com.example.vongtay.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

    @Bean SecurityFilterChain filterChain(HttpSecurity http, NamedParameterJdbcTemplate jdbc) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/access-denied", "/css/**", "/health", "/error").permitAll()
                .requestMatchers("/admin/nhan-vien", "/admin/nhan-vien/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/khach-hang/*/trang-thai").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/don-hang-online/*/xac-nhan-hoan-tien").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/admin/san-pham/**", "/admin/thuoc-tinh/**",
                        "/admin/khuyen-mai/**", "/admin/voucher/**").hasRole("ADMIN")
                .requestMatchers("/admin", "/admin/**", "/", "/dashboard").hasAnyRole("ADMIN", "STAFF")
                .anyRequest().authenticated())
            .formLogin(login -> login.loginPage("/login").loginProcessingUrl("/login")
                    .defaultSuccessUrl("/admin/dashboard", true).failureUrl("/login?error").permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
            .exceptionHandling(exception -> exception.accessDeniedPage("/access-denied"))
            .addFilterAfter(new StaffSessionGuard(jdbc), UsernamePasswordAuthenticationFilter.class);
        // CSRF is enabled by default. Never disable it to make POS AJAX work.
        return http.build();
    }
}
