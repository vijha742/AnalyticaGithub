package com.vikas.service;

import com.vikas.model.CodeMetrics;
import com.vikas.model.ReadmeQuality;
import com.vikas.model.TechnicalProfile;

public interface RepositoryAnalyticsService {
    ReadmeQuality analyzeReadmeQuality(String username, String repositoryName, String filename);
    CodeMetrics getCodeMetrics(String username, String repoName);
    TechnicalProfile getTechnicalProfile(String username);
}
