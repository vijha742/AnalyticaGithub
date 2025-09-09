package com.vikas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnologyUsage {
    private String name;
    private TechnologyCategory category;
    private Instant firstUsed;
    private Instant lastUsed;
    private float frequency;
    private int projectCount;
    private ProficiencyLevel proficiencyLevel;

    public TechnologyUsage(String name, TechnologyCategory category) {
        this.name = name;
        this.category = category;
    }

    public void update(Instant createdAt, Instant lastUpdatedAt) {
        if (this.lastUsed == null || this.lastUsed.isBefore(lastUpdatedAt)) {
            this.lastUsed = lastUpdatedAt;
        }
        if (this.firstUsed == null || this.firstUsed.isAfter(createdAt)) {
            this.firstUsed = createdAt;
        }
        this.projectCount++;
    }
}
