package com.example.app.service.auth;

import com.example.app.data.request.AuthRequest;
import com.example.app.data.request.UserCreateRequest;
import com.example.app.data.request.UserUpdateProfileRequest;
import com.example.app.data.response.TokenResponse;
import com.example.app.data.response.UserDetailResponse;
import io.reactivex.rxjava3.core.Single;

public interface IAuthService {

    Single<TokenResponse> authentication(AuthRequest authRequest);

    Single<String> register(UserCreateRequest userCreateRequest);

    Single<UserDetailResponse> getMe();

    Single<UserDetailResponse> updateProfile(UserUpdateProfileRequest request);

}
