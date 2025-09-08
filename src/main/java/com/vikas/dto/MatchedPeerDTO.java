package com.vikas.dto;

import com.vikas.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedPeerDTO {
    private User matchedUser;
    private List<String> matchingAttributes;
    private double matchScore;
}
