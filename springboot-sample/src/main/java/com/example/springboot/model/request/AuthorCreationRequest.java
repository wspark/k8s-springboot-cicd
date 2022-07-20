package com.example.springboot.model.request;

import lombok.Data;

@Data
public class AuthorCreationRequest {
    private String firstName;
    private String lastName;
}