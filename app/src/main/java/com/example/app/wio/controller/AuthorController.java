package com.example.app.wio.controller;

import com.example.app.data.request.AuthorRequest;
import com.example.app.data.response.AuthorResponse;
import com.example.app.service.author.IAuthorService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tnteco.spring.model.DfResponse;
import vn.tnteco.spring.rest.BaseResource;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/author")
public class AuthorController extends BaseResource<AuthorRequest, AuthorResponse, Integer, IAuthorService> {

    @GetMapping
    public Single<DfResponse<AuthorResponse>> getById(@RequestParam Integer id) {
        return service.getById(id).map(DfResponse::ok);
    }

}
