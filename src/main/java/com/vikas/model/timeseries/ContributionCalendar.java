package com.vikas.model.timeseries;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// ISSUE: How should I set the contribution Calendar like a list of daily commits or list of weekly commits which in itself is a list of daily commits.
@Data
@NoArgsConstructor
public class ContributionCalendar {
    private int totalContributions;
    private List<ContributionWeek> weeks;
}
