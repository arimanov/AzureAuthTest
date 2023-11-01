package org.example.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserAuthData {
    public String accessToken;
    public String email;
    public String firstName;
    public String secondName;
}
