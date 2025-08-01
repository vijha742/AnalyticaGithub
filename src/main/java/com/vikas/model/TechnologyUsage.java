package com.vikas.model;

import lombok.Data;

import java.time.Duration;
import java.time.OffsetDateTime;

@Data
public class TechnologyUsage {
    private String name;
    private TechnologyCategory category;
    private OffsetDateTime firstUsed;
    private OffsetDateTime lastUsed;
    private float frequency;
    private int projectCount;
    private ProficiencyLevel proficiencyLevel;

    public TechnologyUsage(String name, TechnologyCategory category) {
        this.name = name;
        this.category = category;
    }

    public void update(OffsetDateTime createdAt, OffsetDateTime lastUpdatedAt) {
        if (this.lastUsed == null || this.lastUsed.isBefore(lastUpdatedAt)) {
            this.lastUsed = lastUpdatedAt;
        }
        if(this.firstUsed == null || this.firstUsed.isAfter(createdAt)) {
            this.firstUsed = createdAt;
        }
        this.projectCount++;
    }
}
