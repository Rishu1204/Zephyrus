package com.example.zephyrus.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LinkedInService {

    private final RestTemplate restTemplate;

    private static final String POST_URL = "https://api.linkedin.com/v2/ugcPosts";
    private static final String USERINFO_URL = "https://api.linkedin.com/v2/userinfo";

    public String postText(String caption, String accessToken) {

        // 1️⃣ Get person URN
        String personUrn = getPersonUrn(accessToken);

        // 2️⃣ Prepare headers (OAuth 2.0)
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Restli-Protocol-Version", "2.0.0");

        // 3️⃣ Raw JSON payload (TEXT POST)
        String payload = """
        {
          "author": "%s",
          "lifecycleState": "PUBLISHED",
          "specificContent": {
            "com.linkedin.ugc.ShareContent": {
              "shareCommentary": {
                "text": "%s"
              },
              "shareMediaCategory": "NONE"
            }
          },
          "visibility": {
            "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
          }
        }
        """.formatted(personUrn, caption);

        HttpEntity<String> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(POST_URL, request, Map.class);

        return response.getBody().get("id").toString();
    }

    // ✅ Uses OpenID Connect userinfo
    private String getPersonUrn(String accessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response =
                restTemplate.exchange(
                        USERINFO_URL,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class
                );

        return "urn:li:person:" + response.getBody().get("sub").toString();
    }
}