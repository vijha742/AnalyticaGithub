package com.vikas.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TechTimeline {
    private List<projectTime> projectTimeList;
    private List<TechnologyTimeline> technologyUsageList;
}

@Data
class projectTime {
    private String name;
    private Date createdAt;
    private Date lastUpdatedAt;
}

@Data
class TechnologyTimeline {
    private String name;
    private Date firstUsed;
    private Date lastUsed;
    private float frequency;
    private int projectCount;
}
