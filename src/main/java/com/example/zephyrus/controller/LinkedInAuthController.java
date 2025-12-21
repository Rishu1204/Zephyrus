package com.example.zephyrus.controller;

import com.example.zephyrus.service.LinkedInService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth/linkedin")
@RequiredArgsConstructor
public class LinkedInAuthController {

    @Value("${linkedin.client-id}")
    private String clientId;

    @Value("${linkedin.client-secret}")
    private String clientSecret;

    @Value("${linkedin.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final LinkedInService linkedInService;

    @GetMapping("/callback")
    public ResponseEntity<?> callback(@RequestParam(required = false) String code,
                                      @RequestParam(required = false) String error) {
        try {
            if (error != null) {
                return ResponseEntity.badRequest().body("OAuth error: " + error);
            }
            if (code == null) {
                return ResponseEntity.badRequest().body("Missing authorization code");
            }
            String tokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<?> tokenRequest = new HttpEntity<>(body, tokenHeaders);
            ResponseEntity<Map> tokenResponse =
                    restTemplate.postForEntity(tokenUrl, tokenRequest, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");
            return ResponseEntity.ok(linkedInService.postText("Hi! This is my automated post for testing.",accessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during LinkedIn OAuth flow: " + e.getMessage());
        }
    }
}
