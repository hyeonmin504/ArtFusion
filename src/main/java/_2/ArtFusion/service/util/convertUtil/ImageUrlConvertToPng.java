package _2.ArtFusion.service.util.convertUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ImageUrlConvertToPng {

    private final WebClient webClient;

    public ImageUrlConvertToPng(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ByteArrayResource> downloadImageAndConvertToPng(String imageUrl) {
        log.info("imageUrl={}", imageUrl);

        return webClient.get()
                .uri(URI.create(imageUrl))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(bytes -> {
                    try (InputStream inputStream = new ByteArrayInputStream(bytes);
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        //이미지 데이터 읽기
                        BufferedImage bufferedImage = ImageIO.read(inputStream);
                        if (bufferedImage == null) {
                            return Mono.error(new IOException("이미지 정보가 없습니다 url:" + imageUrl));
                        }

                        // 확장자를 png로 변환
                        ImageIO.write(bufferedImage, "png", outputStream);
                        return Mono.just(new ByteArrayResource(outputStream.toByteArray()));

                    } catch (IOException e) {
                        return Mono.error(new IOException("url을 이미지로 변환중 예외 발생! url:" + imageUrl, e));
                    }
                });
    }
}