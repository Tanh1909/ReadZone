package com.example.app.data.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    private String name;

    private Integer parentId;

}
