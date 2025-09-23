package com.back.postpilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static com.back.postpilot.controller.LinkedInOAuthController.token;
import static com.back.postpilot.linkedInAuth.Constants.LI_ME_ENDPOINT;
import static com.back.postpilot.linkedInAuth.Constants.USER_AGENT_OAUTH_VALUE;

@Service
public class LinkedInUserService {

    public static final String ME_ENDPOINT = "https://api.linkedin.com/v2/userinfo";  // Updated endpoint
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LinkedInUserService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserId() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_OAUTH_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        String response = restTemplate.exchange(ME_ENDPOINT, HttpMethod.GET,
                new HttpEntity<>(headers), String.class).getBody();

        return objectMapper.readTree(response).get("sub").asText(); // "sub" for OpenID Connect
    }

    public String getProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_OAUTH_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        return restTemplate.exchange(ME_ENDPOINT, HttpMethod.GET,
                new HttpEntity<>(headers), String.class).getBody();
    }
}
