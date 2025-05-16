package com.example.app.data.response;

import com.example.app.data.model.AddressModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import vn.tnteco.spring.data.base.BaseResponse;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class UserDetailResponse extends BaseResponse {

    private Integer id;

    private String fullName;

    private String email;

    private String phone;

    private AddressModel address;

    private String avatar;

    private Boolean isAdmin;
}
