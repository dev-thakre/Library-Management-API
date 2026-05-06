package com.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank(message = "Author name is required")
    private String name;

    @Email(message = "Invalid email format")
    private String email;
}