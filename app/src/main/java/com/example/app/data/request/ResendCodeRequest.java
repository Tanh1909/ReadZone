package com.example.app.data.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendCodeRequest {

    @NotNull
    private String email;

}
