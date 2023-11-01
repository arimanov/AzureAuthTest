package org.example;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Properties;

import org.example.models.AccessTokenCredential;
import org.example.models.UserAuthData;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;

import lombok.SneakyThrows;

public class AzureAuthViaLibs implements AzureAuth {

    @SneakyThrows
    @Override
    public UserAuthData getUserData(Properties properties, String authCode) {
        
        var app = ConfidentialClientApplication.builder(properties.getProperty("auth.clientId"), ClientCredentialFactory.createFromSecret(properties.getProperty("auth.clientSecret")))
            .authority(String.format("https://login.microsoftonline.com/%s", properties.getProperty("auth.tenantId")))
            .build();

        var parameters = AuthorizationCodeParameters.builder(authCode, new URI(properties.getProperty("auth.redirectUrl")))
            .scopes(Collections.singleton("https://graph.microsoft.com/.default"))
            .build();

        var result = app.acquireToken(parameters).join();
        
        var tokenCredential = new AccessTokenCredential(result.accessToken(), result.expiresOnDate().toInstant().atOffset(ZoneOffset.UTC));
        var authProvider = new TokenCredentialAuthProvider(tokenCredential);

        var graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
        var userData = graphClient.me().buildRequest().get();

        return UserAuthData.builder()
            .accessToken(result.accessToken())
            .email(result.account().username())
            .firstName(userData.givenName)
            .secondName(userData.surname)
            .build();
    }
}
