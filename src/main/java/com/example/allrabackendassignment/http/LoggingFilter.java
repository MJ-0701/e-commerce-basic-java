package com.example.allrabackendassignment.http;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(response);

        Instant start = Instant.now();

        String requestBody = new String(wrappedRequest.getInputStream().readAllBytes());
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String ip = request.getRemoteAddr();

        log.info("[HTTP REQUEST] [{}] {}{} from IP: {} \nHeaders: {}\nBody: {}",
                method,
                uri,
                (query != null ? "?" + query : ""),
                ip,
                getHeadersAsString(request),
                requestBody
        );

        chain.doFilter(wrappedRequest, wrappedResponse);

        Instant end = Instant.now();
        long durationMs = Duration.between(start, end).toMillis();

        String responseBody = wrappedResponse.getCapturedBody();

        log.info("[HTTP RESPONSE] [{}] {}{} ({} ms)\nStatus: {}\nHeaders: {}\nBody: {}",
                method,
                uri,
                (query != null ? "?" + query : ""),
                durationMs,
                response.getStatus(),
                getHeadersAsString(response),
                responseBody
        );

        wrappedResponse.copyBodyToResponse();
    }

    private String getHeadersAsString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        request.getHeaderNames().asIterator().forEachRemaining(headerName ->
                sb.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ")
        );
        return sb.toString();
    }

    private String getHeadersAsString(HttpServletResponse response) {
        StringBuilder sb = new StringBuilder();
        response.getHeaderNames().forEach(headerName ->
                sb.append(headerName).append(": ").append(response.getHeader(headerName)).append("; ")
        );
        return sb.toString();
    }
}
