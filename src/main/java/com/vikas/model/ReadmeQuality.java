package com.vikas.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReadmeQuality {
    private int score;
    private boolean hasIntroduction;
    private boolean hasInstallationGuide;
    private boolean hasUsageExamples;
    private boolean hasMaintainerSection;
    private int wordCount;
    private String lastUpdated;

    public static ReadmeQuality createEmptyReadmeQuality() {
        ReadmeQuality readmeQuality = new ReadmeQuality();
        readmeQuality.setScore(0);
        readmeQuality.setHasIntroduction(false);
        readmeQuality.setHasInstallationGuide(false);
        readmeQuality.setHasUsageExamples(false);
        readmeQuality.setHasMaintainerSection(false);
        readmeQuality.setWordCount(0);
        return readmeQuality;
    }
}


