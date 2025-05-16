package com.example.app.data.mapper;

import com.example.app.data.request.AuthorRequest;
import com.example.app.data.response.AuthorResponse;
import com.example.app.data.tables.pojos.Author;
import org.mapstruct.Mapper;
import vn.tnteco.spring.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public abstract class AuthorMapper extends BaseMapper<AuthorRequest, AuthorResponse, Author> {
}
