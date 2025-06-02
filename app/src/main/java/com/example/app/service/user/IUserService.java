package com.example.app.service.user;

import com.example.app.data.request.UserRequest;
import com.example.app.data.response.UserResponse;
import com.example.app.data.response.user.UserStatisticResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.spring.service.IBaseService;

public interface IUserService extends IBaseService<UserRequest, UserResponse, Integer> {

    Single<UserStatisticResponse> getUserStatistic(Long startTime,Long endTime);

}
