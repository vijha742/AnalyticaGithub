package com.vikas.service.impl;

import com.vikas.model.CodeMetrics;
import com.vikas.model.LanguageStats;
import com.vikas.model.ReadmeQuality;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.LinesCalculator;
import com.vikas.utils.QueryManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public CodeMetrics getCodeMetrics(String username, String repoName) {
        Map<String, String> variables = Map.of("owner", username,"name", repoName);
        Map<String, Object> response = gitHubClient.executeQuery(queryManager.getCodeMetrics(), variables);
        CodeMetrics metrics = new CodeMetrics();
        List<LanguageStats> languageStats = new ArrayList<>();
        int totalSize = 0;
        int totalFiles = 0;
        int totalLines = 0;

        if (response != null && response.containsKey("repository")) {
            Map<String, Object> repo = (Map<String, Object>) response.get("repository");
            Map<String, Object> languages = (Map<String, Object>) repo.get("languages");

            if (languages != null) {
                List<Map<String, Object>> edges = (List<Map<String, Object>>) languages.get("edges");
                totalSize = (Integer) languages.get("totalSize");

                for (Map<String, Object> edge : edges) {
                    LanguageStats stats = new LanguageStats();
                    Map<String, Object> node = (Map<String, Object>) edge.get("node");
                    int size = (Integer) edge.get("size");
                    String lang = (String) node.get("name");
                    stats.setLanguage(lang);
                    int langLines = LinesCalculator.calculateLinesOfCode(lang, size);
                    totalLines += langLines;
                    stats.setLinesOfCode(langLines);
                    stats.setPercentage((float) size / totalSize * 100);
                    languageStats.add(stats);
                }
            }

            totalFiles = gitHubClient.getTotalFiles(username,repoName);
            for (LanguageStats stats : languageStats) {
                stats.setFileCount((int)(totalFiles * (stats.getPercentage() / 100.0f)));
            }
            metrics.setLanguageDistribution(languageStats);
            metrics.setTotalLines(totalLines);
            System.out.println(totalFiles);
            metrics.setAverageFileSize(totalFiles > 0 ? totalSize / totalFiles : 0);
            metrics.setComplexityScore(Math.round(calculateComplexityScore(totalSize, totalFiles) * 100));
            List<String> factors = new ArrayList<>();
            if (totalFiles > 100) factors.add("Large number of files");
            if (languageStats.size() > 3) factors.add("Multiple languages");
            if (totalSize > 100000) factors.add("Large codebase");
            if (factors.isEmpty()) factors.add("Standard complexity");
            metrics.setComplexityFactors(factors);
        }
        return metrics;
    }
    // TODO: Check and see whether this formula is a good metric...
    private float calculateComplexityScore(int totalSize, int totalFiles) {
        float sizeScore = Math.min(1.0f, (float) totalSize / 100000);
        float fileScore = Math.min(1.0f, (float) totalFiles / 1000);
        return (sizeScore * 0.7f + fileScore * 0.3f);
    }

}

