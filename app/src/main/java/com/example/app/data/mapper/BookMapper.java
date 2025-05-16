package com.example.app.data.mapper;

import com.example.app.data.request.BookRequest;
import com.example.app.data.response.book.BookDetailResponse;
import com.example.app.data.response.book.BookResponse;
import com.example.app.data.tables.pojos.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.spring.mapper.BaseMapper;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public abstract class BookMapper extends BaseMapper<BookRequest, BookResponse, Book> {

    public abstract BookDetailResponse toDetailResponse(Book book);

    public List<String> mapJSONBToList(JSONB jsonb) {
        if (jsonb == null) return Collections.emptyList();
        return Json.decodeValue(jsonb.data(), new TypeReference<List<String>>() {
        });
    }

    public JSONB mapListToJSONB(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return JSONB.valueOf("[]");
        return JSONB.valueOf(Json.encode(imageUrls));
    }
}
