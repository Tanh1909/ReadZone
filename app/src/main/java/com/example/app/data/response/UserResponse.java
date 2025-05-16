package com.example.app.data.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.BaseResponse;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserResponse extends BaseResponse {
}
