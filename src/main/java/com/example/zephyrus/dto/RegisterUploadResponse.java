package com.example.zephyrus.dto;

import lombok.Data;

@Data
public class RegisterUploadResponse {

    private Value value;

    @Data
    public static class Value {
        private String asset;
        private UploadMechanism uploadMechanism;
    }

    @Data
    public static class UploadMechanism {
        private HttpRequest uploadHttpRequest;
    }

    @Data
    public static class HttpRequest {
        private String uploadUrl;
    }
}

