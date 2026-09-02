package com.kh.wellness.configuration;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
					// 임시 권한 전부 설정
					requests.anyRequest().permitAll();
					/*
					 * // 1. 관리자 전용 경로 (가장 구체적이거나 제한이 강한 것)
					 * requests.requestMatchers("/api/admins/**").hasRole("ADMIN"); // 2. 인증 관련
					 * (로그인/토큰 등) requests.requestMatchers(HttpMethod.POST,
					 * "/api/auth/**").permitAll(); // 3. 회원 관련
					 * requests.requestMatchers(HttpMethod.POST, "/api/members").permitAll();
					 * requests.requestMatchers(HttpMethod.DELETE,
					 * "/api/members/**").authenticated();
					 * requests.requestMatchers(HttpMethod.PATCH,
					 * "/api/members/**").authenticated(); // 4. 게시판 관련 (보드/공지사항 등)
					 * requests.requestMatchers(HttpMethod.POST, "/api/boards/**").authenticated();
					 * requests.requestMatchers(HttpMethod.PATCH, "/api/boards/**").authenticated();
					 * requests.requestMatchers(HttpMethod.DELETE,
					 * "/api/boards/**").authenticated(); requests.requestMatchers(HttpMethod.GET,
					 * "/api/boards/**").permitAll(); requests.requestMatchers(HttpMethod.GET,
					 * "/api/boards").permitAll(); // 5. 공지사항 관련
					 * requests.requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll();
					 * requests.requestMatchers(HttpMethod.GET, "/api/notices").permitAll(); // 6.
					 * 식물 게시판 관련 requests.requestMatchers(HttpMethod.GET,
					 * "/api/plants/**").permitAll(); requests.requestMatchers(HttpMethod.GET,
					 * "/api/plants").permitAll(); // 7. 이미지 관련
					 * requests.requestMatchers("/uploads/**").permitAll(); // 8. 마이페이지 및 센서 관련
					 * requests.requestMatchers("/api/sensors/**").permitAll();
					 * 
					 * requests.requestMatchers(HttpMethod.GET, "/api/mypage/**").authenticated();
					 * 
					 * // 요청 /api/plants get requests.anyRequest().authenticated();
					 */
					

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
			    "https://goodsfinder.store",
			    "http://localhost:5173",
			    "http://127.0.0.1:5173"
			));
		configuration.setAllowedMethods(Arrays.asList("POST", "PATCH", "DELETE", "GET","PUT","OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		
		return source;
	}
	
	
	

}
