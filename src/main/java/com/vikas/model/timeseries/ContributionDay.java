package com.vikas.model.timeseries;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ContributionDay {
    private String date;
    private int contributionCount;
}
