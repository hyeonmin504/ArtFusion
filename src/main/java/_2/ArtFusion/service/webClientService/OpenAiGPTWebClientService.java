package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.scene.SceneImage;
import _2.ArtFusion.repository.jpa.SceneImageRepository;
import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiGPTWebClientService {

    private final SceneImageRepository sceneImageRepository;
    private final com.theokanning.openai.service.OpenAiService openAiService;

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<String> callGptApiCompletion(String prompt) {
        log.info("start callGptApi");
        return Mono.fromCallable(() -> {
            try {
                ChatMessage message = new ChatMessage("user", prompt);
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .messages(Collections.singletonList(message))
                        .model("gpt-4o-mini")
                        .maxTokens(3000)
                        .build();

                ChatCompletionResult result = openAiService.createChatCompletion(request);

                return result.getChoices().get(0).getMessage().getContent();
            } catch (OpenAiHttpException e) {
                log.error("OpenAiHttpException occurred: {}", e.getMessage(), e);
                throw new RuntimeException("OpenAiHttpException occurred", e);
            } catch (Exception e) {
                log.error("Exception occurred while calling OpenAI API: {}", e.getMessage(), e);
                throw new RuntimeException("Exception occurred while calling OpenAI API", e);
            }

        })
        .timeout(Duration.ofSeconds(30)) // 타임아웃 설정
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))) // 재시도 로직 설정, 3번 재시도, 2초 간격으로 백오프
        .doOnError(OpenAiHttpException.class, e -> log.error("OpenAI API 호출 에러", e))
        .doOnError(Exception.class, e -> log.error("기타 에러", e))
        .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    public void generateImage(SceneFormat sceneFormat) {
        SceneImage storage = new SceneImage(sceneFormat.getBackground() + "url",sceneFormat);

        log.info("storage={}",storage.getUrl());
        SceneImage save = sceneImageRepository.save(storage);
    }

    @Transactional
    public void variationImage(String editPrompt) {

    }
}
