package com.org.education_management.util.api;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@MultipartConfig
public class RequestBodyWrapper extends HttpServletRequestWrapper {
    private final byte[] body;

    public RequestBodyWrapper(HttpServletRequest request) throws IOException {
        super(request);
        this.body = readInputStream(request.getInputStream());
    }

    private byte[] readInputStream(InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }

    public String getBody() {
        return new String(this.body, StandardCharsets.UTF_8);
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // No implementation needed
            }
        };
    }
}