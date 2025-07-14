package com.vikas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    public String jwtToken;
    public String refreshToken;
    public String message;
}
