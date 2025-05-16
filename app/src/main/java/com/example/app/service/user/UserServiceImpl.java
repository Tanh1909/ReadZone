package com.example.app.service.user;

import com.example.app.data.mapper.UserMapper;
import com.example.app.data.request.UserRequest;
import com.example.app.data.response.UserResponse;
import com.example.app.data.tables.pojos.User;
import com.example.app.repository.user.IRxUserRepository;
import com.example.app.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends AppService<UserRequest, UserResponse, User, Integer, IRxUserRepository, UserMapper>
        implements IUserService {

}
