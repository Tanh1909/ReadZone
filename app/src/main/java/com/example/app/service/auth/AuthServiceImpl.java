package com.example.app.service.auth;

import com.example.app.data.Keys;
import com.example.app.data.constant.AppConstant;
import com.example.app.data.constant.CacheConstant;
import com.example.app.data.mapper.UserMapper;
import com.example.app.data.request.*;
import com.example.app.data.response.TokenResponse;
import com.example.app.data.response.UserDetailResponse;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.user.IRxUserRepository;
import com.example.app.service.mail.ISendEmailService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import vn.tnteco.cache.store.external.IExternalCacheStore;
import vn.tnteco.common.core.context.SecurityContext;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.SimpleSecurityUser;
import vn.tnteco.common.data.response.AuthResponse;
import vn.tnteco.common.service.JwtService;
import vn.tnteco.common.utils.TimeUtils;

import java.time.LocalDateTime;

import static com.example.app.data.constant.AppErrorResponse.*;
import static vn.tnteco.common.core.template.RxTemplate.rxSchedulerIo;
import static vn.tnteco.common.data.constant.MessageResponse.SUCCESS;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IRxUserRepository userRepository;

    private final JwtService jwtService;

    private final ISendEmailService sendEmailService;

    private final IExternalCacheStore externalCacheStore;

    private final UserMapper userMapper;

    @Override
    public Single<TokenResponse> authentication(AuthRequest authRequest) {
        String email = authRequest.getEmail();
        String password = authRequest.getPassword();
        return userRepository.getByEmail(email, true)
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
        String email = userCreateRequest.getEmail();
        String fullName = userCreateRequest.getFullName();
        return userRepository.existByEmail(email)
                .flatMap(isExist -> {
                    if (isExist) {
                        return Single.error(new ApiException(EMAIL_HAS_EXIST));
                    }
                    handleSendConfirmCode(email, fullName);
                    User user = new User()
                            .setFullName(fullName)
                            .setEmail(email)
                            .setPasswordHash(BCrypt.hashpw(userCreateRequest.getPassword(), BCrypt.gensalt()))
                            .setCreatedAt(LocalDateTime.now())
                            .setIsAdmin(false)
                            .setIsActive(false);
                    return userRepository.insertUpdateOnConfigKey(user, Keys.USERS_EMAIL_KEY)
                            .map(integer -> SUCCESS);
                });
    }

    private void handleSendConfirmCode(String email, String fullName) {
        String registerConfirmCodeKey = CacheConstant.getRegisterConfirmCode(email);
        String generateConfirmationCode = generateConfirmationCode();
        Integer expireConfirmCodeTimeSeconds = AppConstant.EXPIRE_CONFIRM_CODE_TIME_SECONDS;

        externalCacheStore.putObject(registerConfirmCodeKey, generateConfirmationCode, expireConfirmCodeTimeSeconds);
        sendEmailService.sendConfirmationEmailAsync(email, fullName, generateConfirmationCode);
    }

    @Override
    public Single<Boolean> confirmRegisterCode(ConfirmCodeRequest confirmCodeRequest) {
        String email = confirmCodeRequest.getEmail();
        String codeReq = confirmCodeRequest.getCode();
        return rxSchedulerIo(() -> {
            String registerConfirmCodeKey = CacheConstant.getRegisterConfirmCode(email);
            String code = externalCacheStore.getObject(registerConfirmCodeKey, String.class);
            if (!codeReq.equals(code)) {
                throw new ApiException(BUSINESS_ERROR);
            }
            return true;
        }).flatMap(success -> userRepository.getByEmail(email, false)
                .flatMap(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    user.setIsActive(true);
                    return userRepository.update(user.getId(), user)
                            .map(integer -> true);
                }));

    }

    @Override
    public Single<Boolean> resendConfirmCode(String email) {
        return userRepository.getByEmail(email, false)
                .map(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    handleSendConfirmCode(email, user.getFullName());
                    return true;
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

    @Override
    public Single<UserDetailResponse> updateAvatar(UserUpdateAvatarRequest request) {
        SimpleSecurityUser simpleSecurityUser = getSimpleSecurityUser();
        Integer userId = simpleSecurityUser.getId();
        return userRepository.getById(userId)
                .flatMap(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ApiException(USERNAME_NOT_FOUND));
                    user.setAvatar(request.getAvatar());
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

    private String generateConfirmationCode() {
        // Tạo mã 6 ký tự ngẫu nhiên (A-Z0-9)
        return RandomStringUtils
                .randomAlphanumeric(6)
                .toUpperCase();
    }
}
