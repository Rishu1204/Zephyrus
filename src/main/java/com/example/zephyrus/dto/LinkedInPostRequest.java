package com.example.zephyrus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedInPostRequest {
    @NotBlank
    private String caption;

    @NotBlank
    private String accessToken;

}

