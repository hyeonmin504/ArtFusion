package _2.ArtFusion.config.jwt;

import _2.ArtFusion.controller.ResponseForm;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    //아래 구조와 같은 토큰을 수신한 경우 효과적으로 Auth 수행
    //Authorization : Bearer Header.Payload.Signature
    private final TokenProvider tokenProvider;
    private final List<String> excludedPaths;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @Override
    //정해져있는 시그니쳐임.
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        // 경로가 excludedPaths에 포함되면 인증 요구하지 않음 . 수신한 request 에서 토큰 추출
        boolean isExcludedPath = excludedPaths.stream().anyMatch(excludedPath ->
                requestURI.matches(excludedPath.replace("**", ".*"))
        );

        if (isExcludedPath) {
            filterChain.doFilter(request, response);
            return;
        }
        //수신한 request 에서 토큰 추출
        String token = getAccessToken(request);
        if (token != null && tokenProvider.validateToken(token)) {
            //토큰의 유효성 판단 후
            Authentication authentication = tokenProvider.getAuthentication(token);
            //토큰 소유자 권한에 따라 요청 맥락에 권한 부여
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            ResponseForm<Object> responseForm = new ResponseForm<>(HttpStatus.FORBIDDEN, null, "유효하지 않은 토큰입니다.");
            ObjectMapper objectMapper = new ObjectMapper();
            response.getWriter().write(objectMapper.writeValueAsString(responseForm));
            return;
        }
        filterChain.doFilter(request, response);
    }


    private String getAccessToken(HttpServletRequest request) {
        //토큰 전체 값 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            //접두사 제거한 토큰 값 리턴
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
