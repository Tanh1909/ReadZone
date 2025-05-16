package com.example.app.service.author;

import com.example.app.data.constant.AppErrorResponse;
import com.example.app.data.mapper.AuthorMapper;
import com.example.app.data.request.AuthorRequest;
import com.example.app.data.response.AuthorResponse;
import com.example.app.data.tables.pojos.Author;
import com.example.app.repository.author.IRxAuthorRepository;
import com.example.app.service.AppService;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;

@Log4j2
@Service
@RequiredArgsConstructor
public class AuthorServiceImpl
        extends AppService<AuthorRequest, AuthorResponse, Author, Integer, IRxAuthorRepository, AuthorMapper>
        implements IAuthorService {

    @Override
    public Single<AuthorResponse> getById(Integer id) {
        return repository.getById(id)
                .map(authorOptional -> {
                    Author author = authorOptional.orElseThrow(() -> new ApiException(AppErrorResponse.AUTHOR_NOT_FOUND));
                    return mapper.toResponse(author);
                });
    }

}
