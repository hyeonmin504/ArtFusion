package _2.ArtFusion.service.util.convertUtil;

import _2.ArtFusion.exception.TimeOverException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import javax.imageio.ImageIO;

@Component
@Slf4j
public class ImageUrlConvertToPng {

    private final WebClient webClient;

    public ImageUrlConvertToPng(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ByteArrayResource> downloadImageAndConvertToPngMono(String imageUrl) {
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

    public ByteArrayResource downloadImageAndConvertToPng(String imageUrl) {

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(imageUrl).build(true);
        URI uri = uriComponents.toUri();
        log.info("imageUri={}",uri);

        try {
            // WebClient로 동기 호출을 위해 block() 사용
            byte[] imageBytes = webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_OCTET_STREAM)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();  // 비동기에서 동기로 변환

            if (imageBytes == null) {
                throw new IOException("이미지 데이터가 비어있습니다. url: " + imageUrl);
            }

            try (InputStream inputStream = new ByteArrayInputStream(imageBytes);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                // 이미지 데이터 읽기
                BufferedImage bufferedImage = ImageIO.read(inputStream);
                if (bufferedImage == null) {
                    throw new IOException("이미지 정보가 없습니다 url:" + imageUrl);
                }

                // 확장자를 png로 변환
                ImageIO.write(bufferedImage, "png", outputStream);
                return new ByteArrayResource(outputStream.toByteArray());

            } catch (IOException e) {
                log.error("error",e);
                throw new IOException("url을 이미지로 변환 중 예외 발생! url: " + imageUrl, e);
            }

        } catch (Exception e) {
            log.error("error",e);
            throw new TimeOverException("이미지 시간이 만료되었습니다", e);
        }
    }
}