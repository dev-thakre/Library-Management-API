package com.library.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthorResponse {
    private Long id;
    private String name;
    private String email;
}