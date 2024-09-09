package _2.ArtFusion.config;

import _2.ArtFusion.config.jwt.TokenAuthenticationFilter;
import _2.ArtFusion.config.jwt.TokenProvider;
import _2.ArtFusion.repository.jpa.UserRepository;
import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorizeHttpRequests ->
                        authorizeHttpRequests
                                .requestMatchers(HttpMethod.GET,"/api/users/{email}","/api/archives","/api/comments/**").permitAll()  // GET 요청은 인증 없이 접근 허용
                                .requestMatchers(HttpMethod.POST, "/api/users/login", "/api/users/signup","/api/mail/code","/api/story/temporary").permitAll()  // POST 요청은 인증 없이 접근 허용
                                .requestMatchers(HttpMethod.PUT, "/api/cuts/{sceneId}/**","/api/likes/{postId}").permitAll()
                                .anyRequest().authenticated()  // 그 외 모든 경로는 인증 필요
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .cors(withDefaults());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "192.168.207.29:3000",
                "http://192.168.207.29:3000",
                "http://172.30.1.19:3000",
                "http://localhost:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, List.of(
                "/api/story/temporary",
                "/api/users/{email}","/api/archives/**",
                "/api/users/login", "/api/users/signup",
                "/api/cuts/{sceneId}/**","/api/likes/{postId}",
                "/api/comments/**","/api/mail/code"
        ), userRepository); // TokenRepository 전달
    }

    // 일반 초기화 메서드로 변경
    public void configureSessionCookie(ServletContext servletContext) {
        SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
        sessionCookieConfig.setSecure(false);  // Secure 플래그를 비활성화
        sessionCookieConfig.setHttpOnly(true);  // HttpOnly 설정
        sessionCookieConfig.setPath("/");  // 쿠키의 경로 설정
        sessionCookieConfig.setMaxAge(1800);  // 세션 쿠키의 만료 시간 설정 (예: 30분)
    }
}