package com.example.app.data.request;

import com.example.app.data.model.AddressModel;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateProfileRequest {

    @NotNull
    private String fullName;

    private String phone;

    private AddressModel address;

}
