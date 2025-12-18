package com.example.zephyrus.service;

import com.example.zephyrus.dto.RegisterUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class LinkedInService {

    private final RestTemplate restTemplate;

    private static final String REGISTER_UPLOAD_URL = "https://api.linkedin.com/v2/assets?action=registerUpload";
    private static final String POST_URL = "https://api.linkedin.com/v2/ugcPosts";

    public String postImage(MultipartFile image, String caption, String accessToken, String personUrn) throws IOException {

        HttpHeaders headers = authHeaders(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String registerPayload = """
        {
          "registerUploadRequest": {
            "recipes": ["urn:li:digitalmediaRecipe:feedshare-image"],
            "owner": "%s",
            "serviceRelationships": [{
              "relationshipType": "OWNER",
              "identifier": "urn:li:userGeneratedContent"
            }]
          }
        }
        """.formatted(personUrn);

        HttpEntity<String> registerEntity = new HttpEntity<>(registerPayload, headers);
        ResponseEntity<RegisterUploadResponse> registerResponse =
                restTemplate.postForEntity(
                        REGISTER_UPLOAD_URL,
                        registerEntity,
                        RegisterUploadResponse.class
                );

        String uploadUrl = registerResponse.getBody()
                        .getValue()
                        .getUploadMechanism()
                        .getUploadHttpRequest()
                        .getUploadUrl();

        String asset =
                registerResponse.getBody()
                        .getValue()
                        .getAsset();

        // 2️⃣ Upload image binary
        HttpHeaders uploadHeaders = authHeaders(accessToken);
        uploadHeaders.setContentType(MediaType.parseMediaType(image.getContentType()));

        HttpEntity<byte[]> uploadEntity =
                new HttpEntity<>(image.getBytes(), uploadHeaders);

        restTemplate.exchange(uploadUrl, HttpMethod.PUT, uploadEntity, String.class);

        // 3️⃣ Create post
        String postPayload = """
        {
          "author": "%s",
          "lifecycleState": "PUBLISHED",
          "specificContent": {
            "com.linkedin.ugc.ShareContent": {
              "shareCommentary": {
                "text": "%s"
              },
              "shareMediaCategory": "IMAGE",
              "media": [{
                "status": "READY",
                "media": "%s"
              }]
            }
          },
          "visibility": {
            "com.linkedin.ugc.MemberNetworkVisibility": "PUBLIC"
          }
        }
        """.formatted(personUrn, caption, asset);

        HttpEntity<String> postEntity =
                new HttpEntity<>(postPayload, headers);

        ResponseEntity<String> postResponse =
                restTemplate.postForEntity(POST_URL, postEntity, String.class);

        return postResponse.getBody();
    }

    private HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
