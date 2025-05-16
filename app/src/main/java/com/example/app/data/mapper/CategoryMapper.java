package com.example.app.data.mapper;

import com.example.app.data.request.CategoryRequest;
import com.example.app.data.response.CategoryResponse;
import com.example.app.data.tables.pojos.Category;
import org.mapstruct.Mapper;
import vn.tnteco.spring.mapper.BaseMapper;

@Mapper(componentModel = "spring")
public abstract class CategoryMapper extends BaseMapper<CategoryRequest, CategoryResponse, Category> {
}
