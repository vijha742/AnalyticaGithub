package com.vikas.dto;

import com.vikas.model.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    public String jwtToken;
    public String refreshToken;
    public String message;
    public User userData;
}
