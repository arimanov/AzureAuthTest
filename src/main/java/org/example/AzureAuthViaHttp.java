package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.example.models.AuthResponse;
import org.example.models.UserAuthData;

import com.google.gson.Gson;
import com.microsoft.graph.models.User;

import lombok.SneakyThrows;

public class AzureAuthViaHttp implements AzureAuth {
    
    @Override
    public UserAuthData getUserData(Properties properties, String authCode) {
        var authData = getUserTokenFromGraphAPI(authCode, properties.getProperty("auth.redirectUrl"), 
            properties.getProperty("auth.clientId"), properties.getProperty("auth.clientSecret"), properties.getProperty("auth.tenantId"));
        var userData = getUserInfoFromGraphAPI(authData.accessToken);
        return UserAuthData.builder()
            .accessToken(authData.accessToken)
            .email(userData.mail)
            .firstName(userData.givenName)
            .secondName(userData.surname)
            .build();
    }

    @SneakyThrows
    private User getUserInfoFromGraphAPI(String accessToken) {

        var url = new URL("https://graph.microsoft.com/v1.0/me");
        var conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);

        if (conn.getResponseCode() == 200) {
            return processResponseBody(User.class, conn);

        }
        throw new RuntimeException("Failed to retrieve user information from MS Graph API: " + conn.getResponseCode());
    }
    
    @SneakyThrows
    private AuthResponse getUserTokenFromGraphAPI(String auth_code, String redirectUrl, String clientId, String clientSecret, String tenantId) {

        var formData = "grant_type=" + URLEncoder.encode("authorization_code", StandardCharsets.UTF_8)
            + "&scope=" + URLEncoder.encode("User.Read", StandardCharsets.UTF_8)
            + "&redirect_uri=" + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8)
            + "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
            + "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
            + "&code=" + URLEncoder.encode(auth_code, StandardCharsets.UTF_8);


        var url = new URL(String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId));
        var conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        var os = conn.getOutputStream();
        os.write(formData.getBytes());
        os.flush();
        os.close();

        if (conn.getResponseCode() == 200) {
            return processResponseBody(AuthResponse.class, conn);
        }
        
        throw new RuntimeException("Azure auth failed: " + conn.getResponseCode());
    }
    
    @SneakyThrows
    private <T> T processResponseBody(Class<T> type, HttpURLConnection conn) {
        var in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        var response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        String responseBody = response.toString();
        return new Gson().fromJson(responseBody, type);
    }
    
}
