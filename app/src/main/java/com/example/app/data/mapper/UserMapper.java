package com.example.app.data.mapper;

import com.example.app.data.model.AddressModel;
import com.example.app.data.request.UserRequest;
import com.example.app.data.request.UserUpdateProfileRequest;
import com.example.app.data.response.UserDetailResponse;
import com.example.app.data.response.UserResponse;
import com.example.app.data.response.user.UserInfoResponse;
import com.example.app.data.tables.pojos.User;
import org.jooq.JSONB;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import vn.tnteco.common.core.json.Json;
import vn.tnteco.spring.data.base.UserInfo;
import vn.tnteco.spring.mapper.BaseMapper;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class UserMapper extends BaseMapper<UserRequest, UserResponse, User> {

    public abstract UserInfoResponse toUserInfoResponse(User user);

    public abstract List<UserInfo> toUserInfo(List<User> users);

    public abstract UserDetailResponse toUserDetailResponse(User user);

    public abstract void updateToUser(@MappingTarget User user, UserUpdateProfileRequest userUpdateProfileRequest);

    public AddressModel toAddressModel(JSONB jsonb) {
        if (jsonb == null) {
            return null;
        }
        return Json.decodeValue(jsonb.data(), AddressModel.class);
    }

    public JSONB toJSONB(AddressModel addressModel) {
        if (addressModel == null) {
            return null;
        }
        return JSONB.valueOf(Json.encode(addressModel));
    }

}
