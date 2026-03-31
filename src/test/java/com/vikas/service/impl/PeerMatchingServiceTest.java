package com.vikas.service.impl;

import com.vikas.model.LanguageExpertise;
import com.vikas.model.TechnicalProfile;
import com.vikas.model.User;
import com.vikas.repository.UserRepository;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class PeerMatchingServiceTest {

    @Test
    void getLangStats_whenTotalLinesOfCodeIsZero_returnsEmptyMap() {
        UserRepository userRepository = mock(UserRepository.class);
        PeerMatchingService peerMatchingService = new PeerMatchingService(userRepository);

        User user = new User();
        TechnicalProfile technicalProfile = new TechnicalProfile();
        technicalProfile.setPrimaryLanguages(
                List.of(
                        new LanguageExpertise("Java", 0, 0f, null, null, null, 0, null),
                        new LanguageExpertise("Python", 0, 0f, null, null, null, 0, null)));
        user.setTechnicalProfile(technicalProfile);
        user.setGithubUsername("zero-lines-user");

        Map<String, Double> langStats = peerMatchingService.getLangStats(user);

        assertTrue(langStats.isEmpty());
    }
}
