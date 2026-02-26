package com.vikas.service.impl;

import com.vikas.dto.MatchedPeerDTO;
import com.vikas.model.LanguageExpertise;
import com.vikas.model.User;
import com.vikas.repository.UserRepository;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional
public class PeerMatchingService {
    private final UserRepository userRepository;
    // TODO: To add support for more languages add language here...
    private final String[] languages = {
        "Javascript", "HTML", "CSS", "TypeScript", "Java", "SQL", "Shell", "C", "Python"
    };

    public Map<String, Double> getLangStats(User user) {
        Map<String, Double> langMap = new HashMap<>();
        List<LanguageExpertise> langProfile = user.getTechnicalProfile().getPrimaryLanguages();
        int total = 0;
        for (LanguageExpertise val : langProfile) {
            total += val.getLinesOfCode();
        }
        // HACK: Leaving the logic here if the total is 0. Need to complete it.
        for (LanguageExpertise languageExpertise : langProfile) {
            langMap.put(
                    languageExpertise.getLanguage(),
                    ((double) languageExpertise.getLinesOfCode() / total));
        }
        return langMap;
    }

    public double[] toVector(String[] languages, Map<String, Double> langMap) {
        double[] langVector = new double[languages.length];
        for (int i = 0; i < languages.length; i++) {
            langVector[i] = langMap.getOrDefault(languages[i], 0.0);
        }
        return langVector;
    }

    public double cosineSimilarity(double[] baseUser, double[] user2) {
        double dot = 0.0, magA = 0.0, magB = 0.0;
        for (int i = 0; i < baseUser.length; i++) {
            dot += baseUser[i] * user2[i];
            magA += Math.pow(baseUser[i], 2);
            magB += Math.pow(user2[i], 2);
        }
        if (magA == 0 || magB == 0) return 0.0;
        return dot / (Math.sqrt(magA) * Math.sqrt(magB));
    }

    public List<MatchedPeerDTO> getSupplementingUser() {
        List<MatchedPeerDTO> matchedPeers = new ArrayList<>();
        List<User> usersList = userRepository.findAll();
        User authenticatedUser =
                (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String githubUsernameBaseUser = authenticatedUser.getGithubUsername();
        User baseUser = userRepository.findByGithubUsername(githubUsernameBaseUser).orElse(null);
        Map<String, Double> baseUserStats = getLangStats(baseUser);
        double[] baseUserLangVector = toVector(languages, baseUserStats);
        for (User user : usersList) {
            Map<String, Double> userStats = getLangStats(user);
            double[] userLangVector = toVector(languages, userStats);
            matchedPeers.add(
                    MatchedPeerDTO.builder()
                            .matchedUser(user)
                            .matchScore(cosineSimilarity(baseUserLangVector, userLangVector))
                            .build());
        }
        return matchedPeers.stream()
                .sorted(Comparator.comparingDouble(MatchedPeerDTO::getMatchScore).reversed())
                .limit(4)
                .collect(Collectors.toList());
    }
}
