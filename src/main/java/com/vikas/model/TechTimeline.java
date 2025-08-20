package com.vikas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechTimeline {
    private List<projectTime> projectTimeList = new ArrayList<>();
    private List<TechnologyTimeline> technologyUsageList = new ArrayList<>();

    // TODO: Also get the project time list from repo Analysis
    public TechTimeline (TechnicalProfile technicalProfile) {
        for (LanguageExpertise language : technicalProfile.getPrimaryLanguages()) {
            TechnologyTimeline timeline = new TechnologyTimeline();
            timeline.setName(language.getLanguage());
            timeline.setFirstUsed(language.getFirstUsed() != null ? language.getFirstUsed().toLocalDate() : null);
            timeline.setLastUsed(language.getLastUsed() != null ? language.getLastUsed().toLocalDate() : null);
//            timeline.setFrequency(language.getYearsOfExperience());
            timeline.setProjectCount(language.getProjectCount());
            technologyUsageList.add(timeline);
        }
        for (TechnologyUsage frameworks : technicalProfile.getFrameworksUsed()) {
            TechnologyTimeline timeline = new TechnologyTimeline();
            timeline.setName(frameworks.getName());
            timeline.setFirstUsed(frameworks.getFirstUsed() != null ? frameworks.getFirstUsed().toLocalDate() : null);
            timeline.setLastUsed(frameworks.getLastUsed() != null ? frameworks.getLastUsed().toLocalDate() : null);
            timeline.setFrequency(frameworks.getFrequency());
            timeline.setProjectCount(frameworks.getProjectCount());
            technologyUsageList.add(timeline);
        }
        for (TechnologyUsage library : technicalProfile.getLibrariesUsed()) {
            TechnologyTimeline timeline = new TechnologyTimeline();
            timeline.setName(library.getName());
            timeline.setFirstUsed(library.getFirstUsed() != null ? library.getFirstUsed().toLocalDate() : null);
            timeline.setLastUsed(library.getLastUsed() != null ? library.getLastUsed().toLocalDate() : null);
            timeline.setFrequency(library.getFrequency());
            timeline.setProjectCount(library.getProjectCount());
            technologyUsageList.add(timeline);
        }
        for (TechnologyUsage tools : technicalProfile.getToolingPreferences()) {
            TechnologyTimeline timeline = new TechnologyTimeline();
            timeline.setName(tools.getName());
            timeline.setFirstUsed(tools.getFirstUsed() != null ? tools.getFirstUsed().toLocalDate() : null);
            timeline.setLastUsed(tools.getLastUsed() != null ? tools.getLastUsed().toLocalDate() : null);
            timeline.setFrequency(tools.getFrequency());
            timeline.setProjectCount(tools.getProjectCount());
            technologyUsageList.add(timeline);
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class projectTime {
    private String name;
    private LocalDate createdAt;
    private LocalDate lastUpdatedAt;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class TechnologyTimeline {
    private String name;
    private LocalDate firstUsed;
    private LocalDate lastUsed;
    private float frequency;
    private int projectCount;
}
