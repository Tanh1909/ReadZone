package com.example.app.service.category;

import com.example.app.data.mapper.CategoryMapper;
import com.example.app.data.request.CategoryRequest;
import com.example.app.data.response.CategoryResponse;
import com.example.app.data.tables.pojos.Category;
import com.example.app.repository.category.IRxCategoryRepository;
import com.example.app.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl
        extends AppService<CategoryRequest, CategoryResponse, Category, Integer, IRxCategoryRepository, CategoryMapper>
        implements ICategoryService {


}
