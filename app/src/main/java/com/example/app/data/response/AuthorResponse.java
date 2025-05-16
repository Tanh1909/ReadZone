package com.example.app.data.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.BaseResponse;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AuthorResponse extends BaseResponse {

    private Integer id;

    private String name;

    private String biography;

    private Long birthDate;

    private String imageUrl;

}
