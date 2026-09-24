package com.project.hrms.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

      @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {

 String path = request.getServletPath();
System.out.println("PATH: " + path);

// ✅ SKIP JWT for PUBLIC APIs
/*if (path.equals("/api/employees/login")) {
    filterChain.doFilter(request, response);
    return;

}*/
if (path.startsWith("/api/employees/login")) {
    filterChain.doFilter(request, response);
    return;
}

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    String token = authHeader.substring(7);

    try {
        String email = jwtUtil.extractEmail(token);
       String role = jwtUtil.extractRole(token).toUpperCase();
System.out.println("ROLE FROM TOKEN: " + role);
        // ✅ Create authorities
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
    }

    filterChain.doFilter(request, response);
}
}
