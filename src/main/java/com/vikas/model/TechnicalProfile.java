package com.vikas.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TechnicalProfile {
    private List<LanguageExpertise> primaryLanguages = new ArrayList<>();
    private List<TechnologyUsage> frameworksUsed = new ArrayList<>();
    private List<TechnologyUsage> librariesUsed = new ArrayList<>();
    private List<TechnologyUsage> toolingPreferences = new ArrayList<>();
    private float specializationScore;
    private float versatilityScore;
    private float learningRate; // Haven't calculated it
    private float experimentationFrequency; //  Haven't calculated it
}
