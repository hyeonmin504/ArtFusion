package _2.ArtFusion.domain.user;

import _2.ArtFusion.config.jwt.TokenAuthenticationFilter;
import _2.ArtFusion.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableWebSecurity //모든 요청 URL이 스프링 시큐리티의 제어를 받게 하는 어노테이. 이러면 스프링 시큐리티 활성화 됨.
@RequiredArgsConstructor
public class SecurityConfig {
    private final TokenProvider tokenProvider;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests( //요청 인기 여부 결정을 위한 조건 판단
                        (authorizeHttpRequests)->
                                authorizeHttpRequests
                        .requestMatchers( //비회원이어도 접속 허용하는 URL
                                "/",
                                "/api/login",
                                "api/logout",
                                "api/users/signup"
                        ).permitAll()
                        .anyRequest().authenticated() //나머지 모든 URL에 대해서 회원 로그인 요구
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers->headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(tokenAuthenticationFilter(), //토큰을 username, password 보다 먼저 검사한다.
                        UsernamePasswordAuthenticationFilter.class)
                .cors(withDefaults());

        return http.build();

    }

    //바꿔야됨
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://d3ao949apmj1lo.cloudfront.net",
                "https://botox-chat.site",
                "https://www.botox-chat.site",
                "http://localhost:3000")); // 여러 도메인을 한 번에 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    //
    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider, List.of(
                "/",
                "/api/login",
                "api/logout",
                "api/users/signup"
        ));
    }
}
