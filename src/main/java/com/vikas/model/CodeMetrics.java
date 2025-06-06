package com.vikas.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CodeMetrics {
    private int totalLines;
    private List<LanguageStats> languageDistribution = new ArrayList<>();
    private int averageFileSize;
    private int complexityScore;
    private List<String> complexityFactors = new ArrayList<>();
}
