package com.example.app.wio.controller;


import com.example.app.data.request.*;
import com.example.app.data.response.TokenResponse;
import com.example.app.data.response.UserDetailResponse;
import com.example.app.service.auth.IAuthService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.tnteco.spring.model.DfResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/login")
    public Single<DfResponse<TokenResponse>> login(@RequestBody @Valid AuthRequest authRequest) {
        return authService.authentication(authRequest).map(DfResponse::ok);
    }

    @PostMapping("/register")
    public Single<DfResponse<String>> register(@RequestBody @Valid UserCreateRequest userCreateRequest) {
        return authService.register(userCreateRequest).map(DfResponse::ok);
    }

    @PostMapping("/confirm-code")
    public Single<DfResponse<Boolean>> confirmCode(@RequestBody @Valid ConfirmCodeRequest confirmCodeRequest) {
        return authService.confirmRegisterCode(confirmCodeRequest).map(DfResponse::ok);
    }

    @PostMapping("/resend-code")
    public Single<DfResponse<Boolean>> resendConfirmCode(@RequestBody @Valid ResendCodeRequest request) {
        return authService.resendConfirmCode(request.getEmail()).map(DfResponse::ok);
    }

    @GetMapping("/me")
    public Single<DfResponse<UserDetailResponse>> getMe() {
        return authService.getMe().map(DfResponse::ok);
    }


    @PutMapping("/update-profile")
    public Single<DfResponse<UserDetailResponse>> updateProfile(@RequestBody @Valid UserUpdateProfileRequest userUpdateProfileRequest) {
        return authService.updateProfile(userUpdateProfileRequest).map(DfResponse::ok);
    }

    @PutMapping("/update-avatar")
    public Single<DfResponse<UserDetailResponse>> updateAvatar(@RequestBody @Valid UserUpdateAvatarRequest userUpdateAvatarRequest) {
        return authService.updateAvatar(userUpdateAvatarRequest).map(DfResponse::ok);
    }

}
