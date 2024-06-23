package _2.ArtFusion.exception.handler;

import _2.ArtFusion.controller.ResponseForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 사용자 잘못된 입력 요청 예외 처리
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseForm<Map<String, Object>>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        ResponseForm<Map<String, Object>> responseForm = new ResponseForm<>(HttpStatus.BAD_REQUEST, body, ex.getMessage());
        return new ResponseEntity<>(responseForm, HttpStatus.BAD_REQUEST);
    }

    /**
     * 공통 예외 처리
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseForm<Map<String, Object>>> handleBadRequest(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        ResponseForm<Map<String, Object>> responseForm = new ResponseForm<>(HttpStatus.BAD_REQUEST, body, ex.getMessage());
        return new ResponseEntity<>(responseForm, HttpStatus.BAD_REQUEST);
    }

    /**
     * 서버 에러
     * @param ex
     * @param request
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseForm<Map<String, Object>>> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred."); // 사용자에게 일반적인 메시지로 변경
        body.put("path", request.getDescription(false).replace("uri=", ""));

        ResponseForm<Map<String, Object>> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, body, "An unexpected error occurred.");
        return new ResponseEntity<>(responseForm, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}