package com.vikas.service;

import com.vikas.model.CodeMetrics;
import com.vikas.model.ReadmeQuality;
import com.vikas.model.Repository;
import com.vikas.model.TechnicalProfile;

import java.util.List;

public interface RepositoryAnalyticsService {
    List<ReadmeQuality> analyzeReadmeQuality(String username);

    CodeMetrics getCodeMetrics(String username, String repoName);

    TechnicalProfile getTechnicalProfile(String username);
//    List<Repository> getImpactfulRepository(String username);
}
