package com.example.app.service.author;

import com.example.app.data.request.AuthorRequest;
import com.example.app.data.response.AuthorResponse;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.spring.service.IBaseService;

public interface IAuthorService extends IBaseService<AuthorRequest, AuthorResponse, Integer> {

    Single<AuthorResponse> getById(Integer id);

}
