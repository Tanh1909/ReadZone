package com.example.app.data.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.common.data.response.AuthResponse;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TokenResponse extends AuthResponse {

    private String refreshToken;

    public TokenResponse(AuthResponse authResponse) {
        setToken(authResponse.getToken());
        setExpiredAt(authResponse.getExpiredAt());
    }


}