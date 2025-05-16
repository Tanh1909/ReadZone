package com.example.app.wio.controller;

import com.example.app.data.request.UserRequest;
import com.example.app.data.response.UserResponse;
import com.example.app.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tnteco.spring.rest.BaseResource;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController extends BaseResource<UserRequest, UserResponse, Integer, IUserService> {


}
