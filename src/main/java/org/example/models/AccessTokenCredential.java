package org.example.models;

import java.time.OffsetDateTime;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import reactor.core.publisher.Mono;

public class AccessTokenCredential implements TokenCredential {

    private final String accessToken;
    private final OffsetDateTime expiresAt;
    
    public AccessTokenCredential(String accessToken, OffsetDateTime expiresAt) {
       this.accessToken = accessToken;
       this.expiresAt = expiresAt;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        return Mono.just(new AccessToken(accessToken, expiresAt));
    }
}
