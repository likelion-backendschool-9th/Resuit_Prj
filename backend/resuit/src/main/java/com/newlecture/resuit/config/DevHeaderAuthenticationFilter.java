package com.newlecture.resuit.config;

import com.newlecture.resuit.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * local 프로필에서 X-Username 헤더로 본인을 식별할 때 Spring Security 인증도 함께 채운다.
 */
@Component
@ConditionalOnProperty(name = "app.dev-header-auth.enabled", havingValue = "true")
public class DevHeaderAuthenticationFilter extends OncePerRequestFilter {
    private final UserRepository users;

    public DevHeaderAuthenticationFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null
                || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                || SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                    .equals("anonymousUser")) {
            String username = request.getHeader("X-Username");
            if (username != null && !username.isBlank()) {
                users.findByUsername(username).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(
                            user.getUsername(), "N/A",
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole())));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }
        filterChain.doFilter(request, response);
    }
}
