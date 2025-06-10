package com.example.securityjwt.DTO;

import com.example.securityjwt.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private Integer statusCode;
    private String error;
    private String message;
    private String token;
    private String refreshToken;
    private Long expirationTime;
    private String username;
    private String email;
    private String role;
    private String password;
    private boolean isAccountNonLocked;
    private User user;
}
