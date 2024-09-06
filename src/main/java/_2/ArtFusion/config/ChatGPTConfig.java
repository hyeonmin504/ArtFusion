package _2.ArtFusion.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@Slf4j
public class ChatGPTConfig {

    @Value("${spring.ai.openai.api-key}")
    private String token;

    @Bean
    public OpenAiService openAiService() {
        log.info("token={}가 openaiService을 생성합니다", token);
        return new OpenAiService(token, Duration.ofSeconds(60));
    }
}
