package com.example.allrabackendassignment.global.http;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream capturedOutputStream = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream = new ServletOutputStream() {
        public void write(int b) {
            capturedOutputStream.write(b);
        }

        public boolean isReady() {
            return true;
        }

        public void setWriteListener(WriteListener writeListener) {}
    };

    private final PrintWriter writer = new PrintWriter(capturedOutputStream);

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    public String getCapturedBody() {
        writer.flush();
        return capturedOutputStream.toString();
    }

    public void copyBodyToResponse() throws IOException {
        getResponse().getOutputStream().write(capturedOutputStream.toByteArray());
        getResponse().getOutputStream().flush();
    }
}
