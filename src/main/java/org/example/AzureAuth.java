package org.example;

import java.util.Properties;

import org.example.models.UserAuthData;

public interface AzureAuth {
    UserAuthData getUserData(Properties properties, String authCode);
}
