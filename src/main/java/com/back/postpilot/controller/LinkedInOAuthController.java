package com.back.postpilot.controller;

/*
 * Getting Started with LinkedIn's OAuth APIs ,
 * Documentation: https://docs.microsoft.com/en-us/linkedin/shared/authentication/authentication?context=linkedin/context
 * https://learn.microsoft.com/en-us/linkedin/consumer/integrations/self-serve/sign-in-with-linkedin-v2?context=linkedin%2Fconsumer%2Fcontext
 * https://learn.microsoft.com/en-us/linkedin/shared/authentication/authentication
 * https://learn.microsoft.com/en-us/linkedin/shared/authentication/postman-getting-started
 * https://www.linkedin.com/developers/apps/225222006/auth
 */

import com.back.postpilot.linkedInAuth.AccessToken;
import com.back.postpilot.linkedInAuth.ScopeBuilder;
import com.back.postpilot.service.LinkedInOAuthService;
import com.back.postpilot.service.LinkedInUserService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.back.postpilot.linkedInAuth.Constants.*;
import static com.back.postpilot.linkedInAuth.util.Constants.REQUEST_TOKEN_URL;
import static com.back.postpilot.linkedInAuth.util.Constants.TOKEN_INTROSPECTION_URL;

@RequestMapping("/linkedin")
@RestController
public final class LinkedInOAuthController {

    private final RestTemplate restTemplate; // Changed to final and use constructor injection

    // Define all inputs in the property file
    private Properties prop = new Properties();
    private String propFileName = "config.properties";
    public static String token = null;
    public String refresh_token = null;
    public LinkedInOAuthService service;
    public LinkedInUserService linkedInUserService;

    private Logger logger = Logger.getLogger(LinkedInOAuthController.class.getName());

    // Constructor injection to avoid circular dependency
    public LinkedInOAuthController(RestTemplate restTemplate, LinkedInUserService linkedInUserService) {
        this.restTemplate = restTemplate;
        this.linkedInUserService = linkedInUserService;
    }

    /**
     * Make a Login request with LinkedIN Oauth API
     *
     * @param code optional Authorization code
     * @return Redirects to the client UI after successful token creation
     */
    @RequestMapping(value = "/login")
    public RedirectView oauth(@RequestParam(name = "code", required = false) final String code) throws Exception {

        loadProperty();

        // Construct the LinkedInOAuthService instance for use
        service = new LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(prop.getProperty("clientId"))
                .apiSecret(prop.getProperty("clientSecret"))
                .defaultScope(new ScopeBuilder(prop.getProperty("scope").split(",")).build()) // replace with desired scope
                .callback(prop.getProperty("redirectUri"))
                .build();

        final String secretState = "secret" + new Random().nextInt(999_999);
        final String authorizationUrl = service.createAuthorizationUrlBuilder()
                .state(secretState)
                .build();

        RedirectView redirectView = new RedirectView();

        if (code != null && !code.isEmpty()) {

            logger.log(Level.INFO, "Authorization code not empty, trying to generate a 3-legged OAuth token.");

            final AccessToken[] accessToken = {
                    new AccessToken()
            };
            HttpEntity request = service.getAccessToken3Legged(code);
            String response = restTemplate.postForObject(REQUEST_TOKEN_URL, request, String.class);
            accessToken[0] = service.convertJsonTokenToPojo(response);

            prop.setProperty("token", accessToken[0].getAccessToken());
            token = accessToken[0].getAccessToken();
            refresh_token = accessToken[0].getRefreshToken();

            logger.log(Level.INFO, "Generated Access token and Refresh Token.");

            redirectView.setUrl(prop.getProperty("client_url"));
        } else {
            redirectView.setUrl(authorizationUrl);
        }
        return redirectView;
    }

    /**
     * Create 2 legged auth access token
     *
     * @return Redirects to the client UI after successful token creation
     */
    @RequestMapping(value = "/twoLeggedAuth")
    public RedirectView two_legged_auth() throws Exception {
        loadProperty();

        RedirectView redirectView = new RedirectView();
        // Construct the LinkedInOAuthService instance for use
        service = new LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(prop.getProperty("clientId"))
                .apiSecret(prop.getProperty("clientSecret"))
                .defaultScope(new ScopeBuilder(prop.getProperty("scope").split(",")).build())
                .callback(prop.getProperty("redirectUri"))
                .build();

        final AccessToken[] accessToken = {
                new AccessToken()
        };

        HttpEntity request = service.getAccessToken2Legged();
        String response = restTemplate.postForObject(REQUEST_TOKEN_URL, request, String.class);
        accessToken[0] = service.convertJsonTokenToPojo(response);
        prop.setProperty("token", accessToken[0].getAccessToken());
        token = accessToken[0].getAccessToken();

        logger.log(Level.INFO, "Generated Access token.");

        redirectView.setUrl(prop.getProperty("client_url"));
        return redirectView;
    }

    /**
     * Make a Token Introspection request with LinkedIN API
     *
     * @return check the Time to Live (TTL) and status (active/expired) for all token
     */
    @RequestMapping(value = "/tokenIntrospection")
    public String token_introspection() throws Exception {
        if (service != null) {
            HttpEntity request = service.introspectToken(token);
            String response = restTemplate.postForObject(TOKEN_INTROSPECTION_URL, request, String.class);
            logger.log(Level.INFO, "Token introspected. Details are {0}", response);

            return response;
        } else {
            return TOKEN_INTROSPECTION_ERROR_MESSAGE;
        }
    }

    /**
     * Make a Refresh Token request with LinkedIN API
     *
     * @return get a new access token when your current access token expire
     */
    @RequestMapping(value = "/refreshToken")
    public String refresh_token() throws IOException {
        String response = null;
        if (refresh_token != null) {
            HttpEntity request = service.getAccessTokenFromRefreshToken(refresh_token);
            response = restTemplate.postForObject(REQUEST_TOKEN_URL, request, String.class);
            logger.log(Level.INFO, "Used Refresh Token to generate a new access token successfully.");
            return response;
        } else {
            logger.log(Level.INFO, "Refresh Token cannot be empty. Generate 3L Access Token and Retry again.");
            return response;
        }
    }

    /**
     * Make a Public profile request with LinkedIN API
     *
     * @return Public profile of user
     */
    @RequestMapping(value = "/profile")
    public String profile() throws Exception {
        return linkedInUserService.getProfile();
    }

    /**
     * Get authorization URL for React frontend
     * @return JSON response with authorization URL
     */
    @RequestMapping(value = "/auth-url")
    public String getAuthUrl() throws Exception {
        loadProperty();

        service = new LinkedInOAuthService.LinkedInOAuthServiceBuilder()
                .apiKey(prop.getProperty("clientId"))
                .apiSecret(prop.getProperty("clientSecret"))
                .defaultScope(new ScopeBuilder(prop.getProperty("scope").split(",")).build())
                .callback(prop.getProperty("redirectUri"))
                .build();

        final String secretState = "secret" + new Random().nextInt(999_999);
        final String authorizationUrl = service.createAuthorizationUrlBuilder()
                .state(secretState)
                .build();

        return "{\"authUrl\":\"" + authorizationUrl + "\"}";
    }

    /**
     * Check if user is authenticated
     * @return JSON response with authentication status
     */
    @RequestMapping(value = "/auth-status")
    public String getAuthStatus() {
        if (token != null && !token.isEmpty()) {
            return "{\"authenticated\":true,\"hasToken\":true}";
        } else {
            return "{\"authenticated\":false,\"hasToken\":false}";
        }
    }

    private void loadProperty() throws IOException {
        InputStream inputStream = LinkedInOAuthController.class.getClassLoader().getResourceAsStream(propFileName);
        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
    }
}