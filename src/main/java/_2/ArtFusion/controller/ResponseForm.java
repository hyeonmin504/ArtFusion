package _2.ArtFusion.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Data
@Slf4j
@AllArgsConstructor
public class ResponseForm<T> {
    private HttpStatus code;
    private T data;
    private String message;

    public static <T> ResponseForm<T> success(T data) {
        return new ResponseForm<>(HttpStatus.OK, data, "Ok");
    }

    public static <T> ResponseForm<T> success(T data, String message) {
        return new ResponseForm<>(HttpStatus.OK, data, message);
    }

    public static ResponseForm<Object> unauthorizedResponse(String message) {
        return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, message);
    }

    public static ResponseForm<Object> notFoundResponse(String message) {
        return new ResponseForm<>(HttpStatus.NOT_FOUND, null, message);
    }

    public static ResponseForm<Object> notAcceptResponse(String message) {
        return new ResponseForm<>(NOT_ACCEPTABLE, null, message);
    }

    public static ResponseForm<Object> requestTimeOutResponse(String message) {
        return new ResponseForm<>(REQUEST_TIMEOUT, null, message);
    }

    private Mono<ResponseEntity<ResponseForm<Object>>> handleEditError(Long sceneId, Throwable e) {
        log.error("Error editing content for sceneId={}: {}", sceneId, e.getMessage());
        ResponseForm<Object> body = new ResponseForm<>(INTERNAL_SERVER_ERROR, null, "장면 내용 수정 중 오류가 발생했습니다.");
        return Mono.just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body(body));
    }
}
