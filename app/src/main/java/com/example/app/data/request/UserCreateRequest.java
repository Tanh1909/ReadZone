package com.example.app.data.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotNull
    private String fullName;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 8)
    private String password;

}
