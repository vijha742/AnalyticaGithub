package com.vikas.model;

import lombok.Data;

import java.time.Duration;
import java.time.OffsetDateTime;

@Data
public class LanguageExpertise {
    private String language = "";
    private int linesOfCode;
    private float yearsOfExperience;
    private OffsetDateTime lastUsed;
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.BEGINNER;
    private int projectCount;
    // TODO: Is it really required...
    private TrendIndicator trendIndicator = TrendIndicator.STABLE;

    public LanguageExpertise(String language) {
        this.language = language;
    }

    public void update(OffsetDateTime createdAt, OffsetDateTime lastUpdatedAt, int linesOfCode) {
        if (this.lastUsed == null || this.lastUsed.isBefore(lastUpdatedAt)) {
            this.lastUsed = lastUpdatedAt;
        }
        if (this.lastUsed != null && createdAt != null) {
            Duration experienceDuration = Duration.between(createdAt, this.lastUsed);
            float calculatedYears = experienceDuration.toDays() / 365.25f;
            if (calculatedYears > this.yearsOfExperience) {
                this.yearsOfExperience = calculatedYears;
            }
        }
        this.projectCount++;
        this.linesOfCode += linesOfCode;
        // TODO: think about this metric...
        if (this.yearsOfExperience > 5 && projectCount > 10) {
            this.proficiencyLevel = ProficiencyLevel.EXPERT;
        } else if (this.yearsOfExperience > 3 && projectCount > 5) {
            this.proficiencyLevel = ProficiencyLevel.ADVANCED;
        } else if (this.yearsOfExperience > 1 && projectCount > 2) {
            this.proficiencyLevel = ProficiencyLevel.INTERMEDIATE;
        } else {
            this.proficiencyLevel = ProficiencyLevel.BEGINNER;
        }
    }
}
