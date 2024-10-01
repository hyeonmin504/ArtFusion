package _2.ArtFusion.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${spring.ai.openai.base-url}")
    private String openAiBaseUrl;

    ReactorClientHttpConnector httpConnector = new ReactorClientHttpConnector(
            HttpClient.create()
                    //netty서버와 연결하기 전 time out 설정
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .responseTimeout(Duration.ofSeconds(120))
                    //netty서버와 연결한 후 time out 설정
                    .doOnConnected(conn -> conn
                            .addHandlerLast(new ReadTimeoutHandler(300))
                            .addHandlerLast(new WriteTimeoutHandler(300)))
    );

    // 30 * 1024 = 30KB 대략 1만자, 메모리 사용량을 제한
    ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(30 * 1024))
            .build();

    @Bean
    public WebClient customWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .clientConnector(httpConnector)
                .exchangeStrategies(exchangeStrategies)
                .baseUrl(openAiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
