package _2.ArtFusion.config.jwt;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.repository.jpa.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;


//- **`TokenAuthenticationFilter`**: 이 클래스는 HTTP 요청에서 JWT 토큰을 추출하고, 유효성을 검증한 후, 해당 토큰에 기반하여 Spring Security의 인증을 처리합니다.
//TokenAuthenticationFilter는 tokenprovider를 받아서 Authorization
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final List<String> excludedPaths;
    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 요청 URL을 가져옵니다.
        String requestURI = request.getRequestURI();

        // 토큰 검사가 필요 없는 경로를 설정합니다.
        List<String> excludedPaths = List.of("/api/users/signup", "/api/users/login", "/api/users/logout");

        // 해당 경로에 대해서는 필터링을 건너뜁니다.
        if (excludedPaths.contains(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getAccessToken(request);

        try {
            if (token != null) {
                if (tokenProvider.validateToken(token)) {
                    // 토큰이 유효한지 확인하고 유저가 존재하는지 확인
                    Authentication authentication = tokenProvider.getAuthentication(token);
                    if (authentication != null) {
                        // 인증이 성공하면 Spring Security의 SecurityContext에 인증 정보를 설정합니다.
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        // 인증 정보를 추출할 수 없는 경우
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token is invalid or expired");
                        return;
                    }
                } else {
                    // 유효하지 않은 토큰인 경우
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
                    return;
                }
            } else {
                // 토큰이 없는 경우
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "No token provided");
                return;
            }
        } catch (ExpiredJwtException e) {
            // 토큰이 만료된 경우
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired. Please log in again.");
            return;
        } catch (MalformedJwtException e) {
            // 토큰이 잘못된 형식인 경우
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Malformed JWT token: " + e.getMessage());
            return;
        } catch (JwtException e) {
            // 기타 JWT 관련 오류
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token: " + e.getMessage());
            return;
        }

        // 다음 필터로 요청을 전달합니다.
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ResponseForm<Object> responseForm = new ResponseForm<>(HttpStatus.FORBIDDEN, null, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(responseForm));
    }
}