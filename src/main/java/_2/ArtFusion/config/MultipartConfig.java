package _2.ArtFusion.config;

import _2.ArtFusion.service.util.convertUtil.ImageUrlConvertToPng;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MultipartConfig {

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(openAiBaseUrl)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB로 설정
                        .build())
                .build();
    }

    @Bean
    public ImageUrlConvertToPng imageUrlConvertToPng(WebClient webClient) {
        return new ImageUrlConvertToPng(webClient);
    }
}