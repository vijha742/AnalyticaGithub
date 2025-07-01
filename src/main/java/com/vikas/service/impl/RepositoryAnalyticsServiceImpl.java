package com.vikas.service.impl;

import com.vikas.model.*;
import com.vikas.service.RepositoryAnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.LinesCalculator;
import com.vikas.utils.QueryManager;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
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
    public List<ReadmeQuality> analyzeReadmeQuality(String username) {
        Map<String, String> variables = Map.of("owner", username);
        Map<String, Object> response =
                gitHubClient.executeQuery(queryManager.analyzeReadmeQuality(), variables);
        List<ReadmeQuality> readmeQualities = new ArrayList<>();

        if (response != null && response.containsKey("user")) {
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            Map<String, Object> repositories = (Map<String, Object>) user.get("repositories");
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) repositories.get("nodes");

            for (Map<String, Object> repo : nodes) {
                if ((Boolean) repo.get("isFork")) continue;
                ReadmeQuality quality = new ReadmeQuality();

                String repoName = (String) repo.get("name");
                quality.setTitle(repoName); // Optional: if you want repo info in the result

                Map<String, Object> readme1 = (Map<String, Object>) repo.get("readme1");
                Map<String, Object> readme2 = (Map<String, Object>) repo.get("readme2");
                Map<String, Object> readme3 = (Map<String, Object>) repo.get("readme3");
                Map<String, Object> readme4 = (Map<String, Object>) repo.get("readme4");
                if (readme1 != null) {
                    String readmeContent = (String) readme1.get("text");
                    quality = analyzeReadmeContent(readmeContent);
                    quality.setTitle(repoName);
                } else if (readme2 != null) {
                    String readmeContent = (String) readme2.get("text");
                    quality = analyzeReadmeContent(readmeContent);
                    quality.setTitle(repoName);
                } else if (readme3 != null) {
                    String readmeContent = (String) readme3.get("text");
                    quality = analyzeReadmeContent(readmeContent);
                    quality.setTitle(repoName);
                } else if (readme4 != null) {
                    String readmeContent = (String) readme4.get("text");
                    quality = analyzeReadmeContent(readmeContent);
                    quality.setTitle(repoName);
                }
                Map<String, Object> defaultBranch =
                        (Map<String, Object>) repo.get("defaultBranchRef");
                if (defaultBranch != null) {
                    Map<String, Object> target = (Map<String, Object>) defaultBranch.get("target");
                    if (target != null) {
                        Map<String, Object> history = (Map<String, Object>) target.get("history");
                        if (history != null) {
                            List<Map<String, Object>> historyNodes =
                                    (List<Map<String, Object>>) history.get("nodes");
                            if (historyNodes != null && !historyNodes.isEmpty()) {
                                String lastCommitDate =
                                        (String) historyNodes.get(0).get("committedDate");
                                quality.setLastUpdated(lastCommitDate);
                            }
                        }
                    }
                }
                readmeQualities.add(quality);
            }
        }
        return readmeQualities;
    }

    private ReadmeQuality analyzeReadmeContent(String content) {
        if (content == null) {
            return ReadmeQuality.createEmptyReadmeQuality();
        }
        ReadmeQuality quality = new ReadmeQuality();
        int score = 0;
        boolean hasIntro =
                Pattern.compile("^\\s*#.*?\\n.*?\\n", Pattern.MULTILINE).matcher(content).find();
        quality.setHasIntroduction(hasIntro);
        if (hasIntro) score += 25;
        boolean hasInstall =
                Pattern.compile(
                                "(?i)(installation|getting started|setup|how to use)",
                                Pattern.MULTILINE)
                        .matcher(content)
                        .find();
        quality.setHasInstallationGuide(hasInstall);
        if (hasInstall) score += 25;
        boolean hasExamples =
                Pattern.compile(
                                "(?i)(usage|example|examples|how to|usage example)",
                                Pattern.MULTILINE)
                        .matcher(content)
                        .find();
        quality.setHasUsageExamples(hasExamples);
        if (hasExamples) score += 25;
        boolean hasMaintainer =
                Pattern.compile(
                                "(?i)(contributing|maintainer|contact|support|license)",
                                Pattern.MULTILINE)
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

    @Override
    public List<CodeMetrics> getCodeMetrics(String username) {
        Map<String, String> variables = Map.of("login", username);
        Map<String, Object> response =
                gitHubClient.executeQuery(queryManager.getCodeMetrics(), variables);
        List<CodeMetrics> allMetrics = new ArrayList<>();
        if (response != null && response.containsKey("user")) {
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            Map<String, Object> repositories = (Map<String, Object>) user.get("repositories");
            List<Map<String, Object>> nodes = (List<Map<String, Object>>) repositories.get("nodes");
            for (Map<String, Object> repo : nodes) {
                if ((Boolean) repo.get("isFork")) continue;
                CodeMetrics metrics = new CodeMetrics();
                String repoName = (String) repo.get("name");
                metrics.setTitle(repoName);
                List<LanguageStats> languageStats = new ArrayList<>();
                int totalSize = 0;
                int totalLines = 0;
                Map<String, Object> languages = (Map<String, Object>) repo.get("languages");

                if (languages != null) {
                    List<Map<String, Object>> edges =
                            (List<Map<String, Object>>) languages.get("edges");
                    totalSize = (Integer) languages.get("totalSize");

                    for (Map<String, Object> edge : edges) {
                        int size = (Integer) edge.get("size");
                        if (size > 500000) continue;
                        LanguageStats stats = new LanguageStats();
                        Map<String, Object> node = (Map<String, Object>) edge.get("node");
                        String lang = (String) node.get("name");
                        stats.setLanguage(lang);
                        int langLines = LinesCalculator.calculateLinesOfCode(lang, size);
                        totalLines += langLines;
                        stats.setLinesOfCode(langLines);
                        stats.setPercentage((float) size / totalSize * 100);
                        languageStats.add(stats);
                    }
                }
                                    int totalFiles = 5000;
//                int totalFiles = gitHubClient.getTotalFiles(username, repoName);
                for (LanguageStats stats : languageStats) {
                    stats.setFileCount((int) (totalFiles * (stats.getPercentage() / 100.0f)));
                }
                metrics.setLanguageDistribution(languageStats);
                metrics.setTotalLines(totalLines);
                metrics.setAverageFileSize(totalFiles > 0 ? totalSize / totalFiles : 0);
                metrics.setComplexityScore(
                        Math.round(calculateComplexityScore(totalSize, totalFiles) * 100));
                List<String> factors = new ArrayList<>();
                if (totalFiles > 100) factors.add("Large number of files");
                if (languageStats.size() > 3) factors.add("Multiple languages");
                if (totalSize > 100000) factors.add("Large codebase");
                if (factors.isEmpty()) factors.add("Standard complexity");
                metrics.setComplexityFactors(factors);
                allMetrics.add(metrics);
            }
        }
        return allMetrics;
    }

    // TODO: Check and see whether this formula is a good metric...
    private float calculateComplexityScore(int totalSize, int totalFiles) {
        float sizeScore = Math.min(1.0f, (float) totalSize / 100000);
        float fileScore = Math.min(1.0f, (float) totalFiles / 1000);
        return (sizeScore * 0.7f + fileScore * 0.3f);
    }

    // TODO: apply the filter to not get in languages the repos which are forked...
    @Override
    public TechnicalProfile getTechnicalProfile(String username) {
        TechnicalProfile profile = new TechnicalProfile();
        Map<String, LanguageExpertise> languageMap = new HashMap<>();
        Map<String, TechnologyUsage> frameworkMap = new HashMap<>();

        Map<String, String> variables = Map.of("owner", username);
        Map<String, Object> response =
                gitHubClient.executeQuery(queryManager.getTechnicalProfile(), variables);
        if (response == null) return profile;
        Map<String, Object> user = (Map<String, Object>) response.get("user");
        if (user == null || !user.containsKey("repositories")) return profile;

        Map<String, Object> repositories = (Map<String, Object>) user.get("repositories");
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) repositories.get("nodes");

        if (nodes == null) return profile;

        for (Map<String, Object> repo : nodes) {
            if ((Boolean) repo.get("isFork")) continue;
            String repoName = (String) repo.get("name");
            String createdAtStr = (String) repo.get("createdAt");
            OffsetDateTime createdAt =
                    createdAtStr != null ? OffsetDateTime.parse(createdAtStr) : null;
            String updatedAtStr = (String) repo.get("updatedAt");
            OffsetDateTime updatedAt =
                    updatedAtStr != null ? OffsetDateTime.parse(updatedAtStr) : null;

            Map<String, Object> topicsList = (Map<String, Object>) repo.get("repositoryTopics");
            List<Map<String, Object>> nodesList =
                    (List<Map<String, Object>>) topicsList.get("nodes");

            for (Map<String, Object> nodeSet : nodesList) {
                Map<String, Object> topicMap = (Map<String, Object>) nodeSet.get("topic");
                String topic = topicMap != null ? (String) topicMap.get("name") : null;
                if (isFramework(topic)) {
                    frameworkMap
                            .computeIfAbsent(
                                    topic,
                                    t -> new TechnologyUsage(topic, TechnologyCategory.FRAMEWORK))
                            .update(createdAt, updatedAt);
                } else if (isLibrary(topic)) {
                    frameworkMap
                            .computeIfAbsent(
                                    topic,
                                    t -> new TechnologyUsage(topic, TechnologyCategory.LIBRARY))
                            .update(createdAt, updatedAt);
                } else if (isTool(topic)) {
                    frameworkMap
                            .computeIfAbsent(
                                    topic, t -> new TechnologyUsage(topic, TechnologyCategory.TOOL))
                            .update(createdAt, updatedAt);
                }
            }
            Map<String, Object> languages = (Map<String, Object>) repo.get("languages");
            List<Map<String, Object>> edges = (List<Map<String, Object>>) languages.get("edges");
            if (edges != null) {
                for (Map<String, Object> edge : edges) {
                    int size = (Integer) edge.get("size");
                    Map<String, Object> langNode = (Map<String, Object>) edge.get("node");
                    if (langNode != null) {
                        String languageName = (String) langNode.get("name");
                        if (languageName != null) {
                            languageMap
                                    .computeIfAbsent(
                                            languageName, k -> new LanguageExpertise(languageName))
                                    .update(
                                            createdAt,
                                            updatedAt,
                                            LinesCalculator.calculateLinesOfCode(
                                                    languageName, size));
                        }
                    }
                }
            }
        }
        // TODO: To populate frameworkMap: - topics based, - dependency parsing for limited
        // projects, - leave at all (Currently topic based)
        profile.setPrimaryLanguages(new ArrayList<>(languageMap.values()));
        profile.setFrameworksUsed(new ArrayList<>(frameworkMap.values()));
        profile.setSpecializationScore(calculateSpecializationScore(languageMap.values()));
        profile.setVersatilityScore(calculateVersatilityScore(languageMap.size()));

        return profile;
    }

    public float calculateSpecializationScore(Collection<LanguageExpertise> languages) {
        if (languages.isEmpty()) return 0.0f;
        Optional<LanguageExpertise> primaryLanguage =
                languages.stream().max(Comparator.comparingInt(LanguageExpertise::getLinesOfCode));
        if (primaryLanguage.isEmpty()) return 0.0f;
        int totalLines = languages.stream().mapToInt(LanguageExpertise::getLinesOfCode).sum();

        return (float) primaryLanguage.get().getLinesOfCode() / totalLines;
    }

    public float calculateVersatilityScore(int languages) {
        return Math.min(1.0f, (float) (Math.log(languages + 1) / Math.log(2)) / 5);
    }

    public boolean isFramework(String topic) {
        Set<String> frameworks = Set.of();
        return frameworks.contains(topic.toLowerCase());
    }

    public boolean isLibrary(String topic) {
        Set<String> library = Set.of();
        return library.contains(topic.toLowerCase());
    }

    public boolean isTool(String topic) {
        Set<String> tool = Set.of();
        return tool.contains(topic.toLowerCase());
    }

    //    @Override
    //    public List<Repository> getImpactfulRepository(String username) {
    //        Map<String, String> variables = Map.of("owner", username);
    //        Map<String, Object> response =
    // gitHubClient.executeQuery(queryManager.getImpactfulRepository(), variables);
    //        if (response != null && response.containsKey("user")) {
    //            Map<String, Object> user = (Map<String, Object>) response.get("user");
    //            Map<String, Object> repositories = (Map<String, Object>) user.get("repositories");
    //            List<Map<String, Object>> repos = (List<Map<String, Object>>)
    // repositories.get("nodes");
    //
    //        }
    //    }

    //    public RepoData getRepoData(String username) {
    //        RepoData repoData = new RepoData();
    //
    //        return repoData;
    //    }
}

