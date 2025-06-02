package com.example.app.service.user;

import com.example.app.data.mapper.UserMapper;
import com.example.app.data.request.UserRequest;
import com.example.app.data.response.UserResponse;
import com.example.app.data.response.user.UserStatisticResponse;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.user.IRxUserRepository;
import com.example.app.service.AppService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import vn.tnteco.common.utils.TimeUtils;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends AppService<UserRequest, UserResponse, User, Integer, IRxUserRepository, UserMapper>
        implements IUserService {

    @Override
    public Single<UserStatisticResponse> getUserStatistic(Long startTime, Long endTime) {
        LocalDateTime start = TimeUtils.epochMilliToLocalDateTime(startTime);
        LocalDateTime end = TimeUtils.epochMilliToLocalDateTime(endTime);
        return Single.zip(
                repository.countActiveUser(start, end),
                repository.countActive(),
                (value, total) -> new UserStatisticResponse()
                        .setValue(value)
                        .setTotal(total)
        );
    }
}
