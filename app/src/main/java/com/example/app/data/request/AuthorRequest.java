package com.example.app.data.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuthorRequest {

    @NotNull
    private String name;

    private String biography;

    private LocalDateTime birthDate;

    private String imageUrl;

}
