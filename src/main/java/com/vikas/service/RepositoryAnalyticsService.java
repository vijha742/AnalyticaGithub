 package com.vikas.service;


 import com.vikas.model.CodeMetrics;
 import com.vikas.model.ReadmeQuality;
 import com.vikas.model.TechnicalProfile;

 import java.util.List;

 public interface RepositoryAnalyticsService {
 List<ReadmeQuality> analyzeReadmeQuality(String username);
 List<CodeMetrics> getCodeMetrics(String username);
 TechnicalProfile getTechnicalProfile(String username);
 }
