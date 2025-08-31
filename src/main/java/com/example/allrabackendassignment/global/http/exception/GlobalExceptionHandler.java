package com.example.allrabackendassignment.global.http.exception;

import com.example.allrabackendassignment.global.http.exception.InsufficientStockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** trace=true 이면 스택트레이스도 응답에 포함 */
    private static boolean includeTrace(HttpServletRequest req) {
        String trace = req.getParameter("trace");
        return "true".equalsIgnoreCase(trace) || "1".equals(trace);
    }

    private ProblemDetail base(HttpStatus status, String title, String detail, HttpServletRequest req, Throwable ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setProperty("timestamp", OffsetDateTime.now());
        pd.setProperty("path", req.getRequestURI());
        if (includeTrace(req)) {
            pd.setProperty("exception", ex.getClass().getName());
            pd.setProperty("trace", Arrays.stream(ex.getStackTrace()).limit(50).toArray());
        }
        return pd;
    }

    // ====== 도메인/비즈니스 예외 ======
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest req) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        ProblemDetail pd = base(HttpStatus.CONFLICT, "Insufficient Stock", ex.getMessage(), req, ex);
        // ex에 productId/요청/재고 필드 getter가 있으면 아래처럼 추가
        // pd.setProperty("productId", ex.getProductId());
        // pd.setProperty("requested", ex.getRequested());
        // pd.setProperty("available", ex.getAvailable());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        log.warn("Illegal state: {}", ex.getMessage());
        ProblemDetail pd = base(HttpStatus.CONFLICT, "Illegal State", ex.getMessage(), req, ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, ex);
        return ResponseEntity.badRequest().body(pd);
    }

    // ====== 요청/바인딩/검증 관련 ======
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Validation Failed", "입력값 검증에 실패했습니다.", req, ex);
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex, HttpServletRequest req) {
        ProblemDetail pd = base(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req, ex);
        return ResponseEntity.badRequest().body(pd);
    }

    // ====== 데이터 무결성 등 ======
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.error("Data integrity violation", ex);
        ProblemDetail pd = base(HttpStatus.CONFLICT, "Data Integrity Violation", ex.getMostSpecificCause().getMessage(), req, ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
    }

    // ====== 스프링의 ErrorResponseException / ResponseStatusException ======
    @ExceptionHandler({ErrorResponseException.class, ResponseStatusException.class})
    public ResponseEntity<ProblemDetail> handleSpringErrors(RuntimeException ex, HttpServletRequest req) {
        HttpStatusCode status = (ex instanceof ErrorResponseException ere)
                ? ere.getStatusCode()
                : ((ResponseStatusException) ex).getStatusCode();
        ProblemDetail pd = base(HttpStatus.valueOf(status.value()), ex.getClass().getSimpleName(), ex.getMessage(), req, ex);
        return ResponseEntity.status(status).body(pd);
    }

    // ====== 알 수 없는 예외: 500 ======
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnknown(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = base(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "서버 내부 오류가 발생했습니다.", req, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
