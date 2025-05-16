package com.example.app.service;

import vn.tnteco.repository.IRxRepository;
import vn.tnteco.spring.data.base.BaseResponse;
import vn.tnteco.spring.mapper.BaseMapper;
import vn.tnteco.spring.service.BaseServiceImpl;

public abstract class AppService<Rq, Rs extends BaseResponse, Pojo, ID, Repo extends IRxRepository<Pojo, ID>, Mp extends BaseMapper<Rq, Rs, Pojo>>
        extends BaseServiceImpl<Rq, Rs, Pojo, ID, Repo, Mp> {
}
