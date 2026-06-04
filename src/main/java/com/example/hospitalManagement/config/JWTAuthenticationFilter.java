package com.example.hospitalManagement.config;

import com.example.hospitalManagement.service.CustomUserDretailService;
import com.example.hospitalManagement.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private CustomUserDretailService customUserDretailService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path=request.getRequestURI();
        if(path.equals("/login")||path.equals("/auth")||path.startsWith("/api/auth")){
            filterChain.doFilter(request,response);//cho request chạy tiếp xuống controller
            return;
        }
        String token=extractToken(request);
        if(token!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            try {
                if(jwtUtil.validateToken(token)){
                    String type= jwtUtil.extractType(token);
                    if(type.equals("access_token")){
                        String username=jwtUtil.extractUsername(token);
                        UserDetails userDetails= customUserDretailService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,null,userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);//Gắn vào SecurityContext Nó nói với Spring: request này đã authenticated
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);



    }
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("access_token".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
