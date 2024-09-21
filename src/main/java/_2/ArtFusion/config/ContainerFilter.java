package _2.ArtFusion.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;


@Component
public class ContainerFilter implements Filter {

    @Value("${COLOR:unknown}")
    private String containerColor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        // 응답 헤더에 X-Container 추가
        httpServletResponse.addHeader("X-Container", containerColor);
        chain.doFilter(request, response);
    }
}