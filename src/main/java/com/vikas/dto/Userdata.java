package com.vikas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Userdata {
        private final String id;
        private final String email;
        private final String name;
        private final String imageUrl;
        private final String githubId;

}
