package com.example.app.data.response.user;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserInfoResponse {

    private String avatar;

    private String fullName;

}
