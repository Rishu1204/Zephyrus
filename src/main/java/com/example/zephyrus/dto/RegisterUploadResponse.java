package com.example.zephyrus.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

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

        private MediaUploadHttpRequest httpRequest;

        @JsonAnySetter
        public void handleDynamicKey(String key, Object value) {
            if (value instanceof Map<?, ?> map) {
                MediaUploadHttpRequest req = new MediaUploadHttpRequest();
                req.setUploadUrl(map.get("uploadUrl").toString());
                this.httpRequest = req;
            }
        }
    }

    @Data
    public static class MediaUploadHttpRequest {
        private String uploadUrl;
    }
}
