package com.example.app.wio.controller;

import com.example.app.data.request.CategoryRequest;
import com.example.app.data.response.CategoryResponse;
import com.example.app.service.category.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tnteco.spring.rest.BaseResource;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/category")
public class CategoryController
        extends BaseResource<CategoryRequest, CategoryResponse, Integer, ICategoryService> {


}
