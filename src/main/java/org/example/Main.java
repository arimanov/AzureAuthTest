package org.example;

import java.net.URL;
import java.util.Properties;
import java.util.Set;

import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.ResponseMode;

import lombok.SneakyThrows;


public class Main {
    
    private static final String AUTH_CODE = "";
    
    @SneakyThrows
    public static void main(String[] args) {
        
        var properties = initProperties();
        
        // Example 0: Generate auth url
        var authUrl = generateAuthURL(
            String.format("https://login.microsoftonline.com/%s", properties.getProperty("auth.tenantId")), 
            properties.getProperty("auth.redirectUrl"), properties.getProperty("auth.clientId"));
        System.out.println(authUrl);
        
        // Example 1: Get token and data without libs
        var azureAuthViaHttp = new AzureAuthViaHttp();
        var result = azureAuthViaHttp.getUserData(properties, AUTH_CODE);
        System.out.println(result);
        
        // Example 2: Get token and data via libraries
        var azureAuthViaLibs = new AzureAuthViaLibs();
        var result2 = azureAuthViaLibs.getUserData(properties, AUTH_CODE);
        System.out.println(result2);
        
    }
    
    @SneakyThrows
    private static URL generateAuthURL(String loginUrl, String redirectUrl, String clientId) {

        var publicClientApplication = PublicClientApplication.builder(clientId)
            .authority(loginUrl)
            .build();
        
        AuthorizationRequestUrlParameters authRequest = AuthorizationRequestUrlParameters
            .builder(redirectUrl, Set.of("User.Read"))
            .responseMode(ResponseMode.QUERY)
            .prompt(Prompt.SELECT_ACCOUNT)
            .build();
        return publicClientApplication.getAuthorizationRequestUrl(authRequest);
    }

    @SneakyThrows
    private static Properties initProperties() {
        var appProps = new Properties();
        appProps.load(Main.class.getClassLoader().getResourceAsStream("app.properties"));
        return appProps;
    }
}