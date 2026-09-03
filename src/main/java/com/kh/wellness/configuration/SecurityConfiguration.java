package com.kh.wellness.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kh.wellness.configuration.filter.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
	private final JwtFilter jwtFilter;
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

		return http.formLogin(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults()).authorizeHttpRequests(requests ->{
                    // CORS Preflight
		            requests.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

		            // 인증 관련
		            requests.requestMatchers("/api/auth/login").permitAll();
		            requests.requestMatchers("/api/auth/refresh").permitAll();
		            requests.requestMatchers("/api/auth/google").permitAll();

		            // 회원가입 / 이메일 인증
		            requests.requestMatchers(HttpMethod.POST, "/api/members").permitAll();
		            requests.requestMatchers("/api/mail/**").permitAll();

		            // 지도 핀·장소 검색·길찾기는 회원과 비회원 모두 사용하는 프론트 조회 API
		            requests.requestMatchers(HttpMethod.GET,
						"/api/places/pins",
						"/api/routes",
						"/api/routes/origins").permitAll();

		            // 회원 상세 - 로그인 필요
		            requests.requestMatchers("/api/members/detail").authenticated();
		            
		            // 장소 관련 
		            requests.requestMatchers("/api/places/*").permitAll();

		            // 관리자 API - ADMIN만 접근
		            requests.requestMatchers("/api/admin/**").hasRole("ADMIN");

		            // 그 외 모든 API - 로그인 필요
		            requests.anyRequest().authenticated();

				}).sessionManagement(manager ->
				manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
				.build();
		}

	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(
			    "http://localhost:5173"
			));

		configuration.setAllowedMethods(Arrays.asList("POST", "PATCH", "DELETE", "GET","PUT","OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		
		return source;
	}
	
	
	

}
