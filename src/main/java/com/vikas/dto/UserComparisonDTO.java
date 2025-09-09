package com.vikas.dto;

import com.vikas.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserComparisonDTO {
    private CompResults results;
    private User user1;
    private User user2;
}
