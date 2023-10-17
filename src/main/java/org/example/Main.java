package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.graph.models.User;


public class Main {
    
    private static final String TENANT_ID = "";
    private static final String MS_LOGIN_URL = String.format("https://login.microsoftonline.com/%s", TENANT_ID);
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String REDIRECT_URL = "https://localhost";
    private static final String AUTH_CODE = "";
    
    
        
    public static void main(String[] args) throws IOException, URISyntaxException {
        
        // Example 1: Get token without msal4j library
        // var tokenData = getUserTokenFromGraphAPI(AUTH_CODE);
        // var access_token = tokenData.accessToken;
        // System.out.println("--> Access token: " + access_token);
        
        // Example 2: Get token via msal4j library
        var app = ConfidentialClientApplication.builder(CLIENT_ID, ClientCredentialFactory.createFromSecret(CLIENT_SECRET))
            .authority(MS_LOGIN_URL)
            .build();

        AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(AUTH_CODE, new URI(REDIRECT_URL))
            .scopes(Collections.singleton("https://graph.microsoft.com/.default"))
            .build();

        var result = app.acquireToken(parameters).join();
        var access_token = result.accessToken();

        System.out.println("--> Access token: " + access_token);
        System.out.println("--> User email: " + result.account().username());
        System.out.println("--> Expires on: " + result.expiresOnDate());
        
        // Example 3: Get an additional user info via MS Graph API
        var user = getUserInfoFromGraphAPI(access_token);
        System.out.println("--> First Name: " + user.givenName);
        System.out.println("--> Second Name: " + user.surname);
    }

    private static User getUserInfoFromGraphAPI(String accessToken) throws IOException {

        var url = new URL("https://graph.microsoft.com/v1.0/me");
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        int responseCode = conn.getResponseCode();
        
        if (responseCode == 200) {
            var in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            String responseBody = response.toString();
            System.out.println("Response: " + responseBody);
            var gson = new com.google.gson.Gson();
            return gson.fromJson(responseBody, User.class);
            
        } else {
            throw new RuntimeException("Failed to retrieve user information from MS Graph API: " + responseCode);
        }
    }

    private static AuthResponse getUserTokenFromGraphAPI(String auth_code) throws IOException {
        
        var formData = "grant_type=" + URLEncoder.encode("authorization_code", StandardCharsets.UTF_8) 
            + "&scope=" + URLEncoder.encode("User.Read Mail.Read", StandardCharsets.UTF_8)
            + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URL, StandardCharsets.UTF_8)
            + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
            + "&client_secret=" + URLEncoder.encode(CLIENT_SECRET, StandardCharsets.UTF_8)
            + "&code=" + URLEncoder.encode(auth_code, StandardCharsets.UTF_8);


        var url = new URL("https://login.microsoftonline.com/fe54e49e-3b45-4ec7-8d1c-558ade7e6e70/oauth2/v2.0/token");
        var conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        var os = conn.getOutputStream();
        os.write(formData.getBytes());
        os.flush();
        os.close();

        if (conn.getResponseCode() == 200) {
            var in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String responseBody = response.toString();
            var gson = new com.google.gson.Gson();
            return gson.fromJson(responseBody, AuthResponse.class);

        } else {
            throw new RuntimeException("Azure auth failed: " + conn.getResponseCode());
        }
        
    }
}