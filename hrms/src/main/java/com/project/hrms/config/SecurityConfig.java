
package com.project.hrms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
private JwtFilter jwtFilter;
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
     .cors(cors -> {})   // ✅ NEW WAY
    .csrf(csrf -> csrf.disable())
   .authorizeHttpRequests(auth -> auth

            // PUBLIC
        .requestMatchers(HttpMethod.POST, "/api/employees").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/employees/login").permitAll()

        .requestMatchers(HttpMethod.GET, "/api/employees").hasRole("HR")
        //.requestMatchers(HttpMethod.GET, "/api/employees", "/api/employees/").hasRole("HR")
       // .requestMatchers(HttpMethod.GET, "/api/employees", "/api/employees/").authenticated()
        // BOTH (attendance)
        .requestMatchers("/api/employees/my-attendance")
    .hasAnyRole("HR", "EMPLOYEE")
        .requestMatchers(HttpMethod.POST, "/api/employees/attendance").hasAnyRole("HR", "EMPLOYEE")
        .requestMatchers("/api/employees/*/attendance").hasAnyRole("HR", "EMPLOYEE")
        .requestMatchers(
    HttpMethod.PUT,
    "/api/employees/hr/attendance/*"
).hasRole("HR")
     .requestMatchers(HttpMethod.POST, "/api/employees/leave")
.hasAnyRole("HR", "EMPLOYEE")

.requestMatchers(HttpMethod.GET, "/api/employees/*/leaves")
.hasAnyRole("HR", "EMPLOYEE")
.requestMatchers("/api/employees/leave/me")
.hasAnyRole("HR", "EMPLOYEE")
.requestMatchers("/api/employees/leave/**")
.hasRole("HR")

.requestMatchers(HttpMethod.GET, "/api/employees/attendance/all")
.hasRole("HR")
        // HR ONLY
       // .requestMatchers("/api/employees/**").hasRole("HR")
       .requestMatchers(
    HttpMethod.GET,
    "/api/employees/reports/**"
).hasRole("HR")
.requestMatchers(HttpMethod.GET,
        "/api/employees/profile")
.hasAnyRole("HR","EMPLOYEE")
        // DEFAULT
        .anyRequest().authenticated()
    );


http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
}