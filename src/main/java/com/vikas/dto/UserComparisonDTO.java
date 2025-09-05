package com.vikas.dto;


import com.vikas.model.SuggestedUser;
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
    private SuggestedUser user1;
    private SuggestedUser user2;
}
