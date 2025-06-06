package com.vikas.service;

import com.vikas.model.ReadmeQuality;

public interface RepositoryAnalyticsService {
    ReadmeQuality analyzeReadmeQuality(String username, String repositoryName, String filename);
}
