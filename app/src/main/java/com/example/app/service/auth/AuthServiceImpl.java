package com.example.app.service.auth;

import com.example.app.data.mapper.UserMapper;
import com.example.app.data.request.AuthRequest;
import com.example.app.data.request.UserCreateRequest;
import com.example.app.data.request.UserUpdateProfileRequest;
import com.example.app.data.response.TokenResponse;
import com.example.app.data.response.UserDetailResponse;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.user.IRxUserRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.data.response.AuthResponse;
import vn.tnteco.common.service.JwtService;
import vn.tnteco.common.utils.TimeUtils;

import java.time.LocalDateTime;

import static com.example.app.data.constant.AppErrorResponse.*;
import static vn.tnteco.common.data.constant.MessageResponse.SUCCESS;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IRxUserRepository userRepository;

    private final JwtService jwtService;

    private final UserMapper userMapper;

    @Override
    public Single<TokenResponse> authentication(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();
        return userRepository.getByEmail(email)
                .map(userOptional -> {
                    User user = userOptional
                            .orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                        throw new ApiException(PASSWORD_INVALID);
                    }
                    Long issuedAt = TimeUtils.localDateTimeToEpochMilli(LocalDateTime.now());
                    SimpleSecurityUser simpleSecurityUser = new SimpleSecurityUser()
                            .setId(user.getId())
                            .setUsername(user.getEmail())
                            .setIssuedAt(issuedAt);
                    AuthResponse authResponse = jwtService.generateAuthResponse(simpleSecurityUser);
                    return new TokenResponse(authResponse);
                });
    }

    @Override
    public Single<String> register(UserCreateRequest userCreateRequest) {
        return userRepository.existByEmail(userCreateRequest.getEmail())
                .flatMap(isExist -> {
                    if (isExist) {
                        return Single.error(new ApiException(EMAIL_HAS_EXIST));
                    }
                    User user = new User()
                            .setFullName(userCreateRequest.getFullName())
                            .setEmail(userCreateRequest.getEmail())
                            .setPasswordHash(BCrypt.hashpw(userCreateRequest.getPassword(), BCrypt.gensalt()))
                            .setCreatedAt(LocalDateTime.now())
                            .setIsAdmin(false);
                    return userRepository.insert(user)
                            .map(integer -> SUCCESS);
                });
    }

    @Override
    public Single<UserDetailResponse> getMe() {
        SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUser();
        return userRepository.getById(simpleSecurityUser.getId())
                .map(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    return userMapper.toUserDetailResponse(user);
                });
    }

    @Override
    public Single<UserDetailResponse> updateProfile(UserUpdateProfileRequest userUpdateProfileRequest) {
        SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUser();
        Integer userId = simpleSecurityUser.getId();
        return userRepository.getById(userId)
                .flatMap(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    userMapper.updateToUser(user, userUpdateProfileRequest);
                    user.setUpdatedAt(LocalDateTime.now());
                    return userRepository.updateReturning(userId, user)
                            .map(userUpdate -> {
                                User result = userUpdate.orElseThrow(() -> new ApiException(BUSINESS_ERROR));
                                return userMapper.toUserDetailResponse(result);
                            });
                });
    }

    protected SimpleSecurityUser getSimpleSecurityUser() {
        SimpleSecurityUser simpleSecurityUser = SecurityContext.getSimpleSecurityUser();
        if (simpleSecurityUser == null) {
            throw new ApiException(UNAUTHORIZED);
        }
        return simpleSecurityUser;
    }
}
