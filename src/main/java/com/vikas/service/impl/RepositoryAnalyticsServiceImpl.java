package com.vikas.service.impl;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.vikas.model.ReadmeQuality;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class RepositoryAnalyticsServiceImpl implements RepositoryAnalyticsService {
    private final GithubGraphQLClient gitHubClient;
    private final QueryManager queryManager;

    public RepositoryAnalyticsServiceImpl(GithubGraphQLClient gitHubClient) {
        this.queryManager = new QueryManager();
        this.gitHubClient = gitHubClient;
    }

    @Override
    public ReadmeQuality analyzeReadmeQuality(String username, String repositoryName, String filename) {
        ReadmeQuality quality = new ReadmeQuality();
        Map<String, String> variables = Map.of("owner", username,"name", repositoryName,"filePath", filename);
        Map<String, Object> response = gitHubClient.executeQuery(queryManager.analyzeReadmeQuality(), variables);
        if (response != null && response.containsKey("repository")) {
            Map<String, Object> repo = (Map<String, Object>) response.get("repository");
            Map<String, Object> object = (Map<String, Object>) repo.get("object");

            if (object != null) {
                String readmeContent = (String) object.get("text");
                quality = analyzeReadmeContent(readmeContent);
                Map<String, Object> defaultBranch = (Map<String, Object>) repo.get("defaultBranchRef");
                // HACK: Tells last commit date in the repo not the Readme.
                if (defaultBranch != null) {
                    Map<String, Object> target = (Map<String, Object>) defaultBranch.get("target");
                    Map<String, Object> history = (Map<String, Object>) target.get("history");
                    List<Map<String, Object>> nodes = (List<Map<String, Object>>) history.get("nodes");
                    if (!nodes.isEmpty()) {
                        String lastCommitDate = (String) nodes.getFirst().get("committedDate");
                        quality.setLastUpdated(lastCommitDate);
                    }
                }
            }
        }
        return quality;
    }

    private ReadmeQuality analyzeReadmeContent(String content) {
        if (content == null) {
            return ReadmeQuality.createEmptyReadmeQuality();
        }
        ReadmeQuality quality = new ReadmeQuality();
        int score = 0;
        boolean hasIntro = Pattern.compile("^\\s*#.*?\\n.*?\\n", Pattern.MULTILINE)
                .matcher(content)
                .find();
        quality.setHasIntroduction(hasIntro);
        if (hasIntro) score += 25;
        boolean hasInstall = Pattern.compile("(?i)(installation|getting started|setup|how to use)", Pattern.MULTILINE)
                .matcher(content)
                .find();
        quality.setHasInstallationGuide(hasInstall);
        if (hasInstall) score += 25;
        boolean hasExamples = Pattern.compile("(?i)(usage|example|examples|how to|usage example)", Pattern.MULTILINE)
                .matcher(content)
                .find();
        quality.setHasUsageExamples(hasExamples);
        if (hasExamples) score += 25;
        boolean hasMaintainer = Pattern.compile("(?i)(contributing|maintainer|contact|support|license)", Pattern.MULTILINE)
                .matcher(content)
                .find();
        quality.setHasMaintainerSection(hasMaintainer);
        if (hasMaintainer) score += 25;
        int wordCount = content.split("\\s+").length;
        quality.setWordCount(wordCount);
        if (wordCount < 100) score *= 0.5;
        else if (wordCount > 500) score *= 1.2;
        quality.setScore(Math.min(100, score));
        return quality;
    }
}

