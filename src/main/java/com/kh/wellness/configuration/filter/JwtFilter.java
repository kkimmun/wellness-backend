package com.kh.wellness.configuration.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.wellness.auth.model.vo.CustomUserDetails;
import com.kh.wellness.token.util.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request)
            throws ServletException {

        String uri = request.getRequestURI();

        return uri.equals("/api/auth/login")
                || uri.equals("/api/auth/refresh");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        
        log.info("Authorization: {}",
                request.getHeader(HttpHeaders.AUTHORIZATION));

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.parseJwt(token);

            String memberNo = claims.getSubject();
            String role = claims.get("role", String.class);

            CustomUserDetails user = CustomUserDetails.builder()
                    .memberNo(Long.valueOf(memberNo))
                    .authorities(resolveAuthorities(role))
                    .build();

            log.info("로그인한 유저 권한 확인: {}", user.getAuthorities());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("토큰만료");
            return;

        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("유효하지 않은 토큰");
            return;
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("유효하지 않은 토큰");
            return;
        }
        
        

        filterChain.doFilter(request, response);
    }

    // 페이로드의 role claim 을 Spring Security 권한으로 변환한다. (hasRole('ADMIN') 매칭을 위해 ROLE_ 접두어 부여)
    private Collection<? extends GrantedAuthority> resolveAuthorities(String role) {
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
}