package com.vikas.model.timeseries;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@NoArgsConstructor
@Data
public class ContributionWeek {
    private String firstDay;
    private List<ContributionDay> contributionDays;
}
