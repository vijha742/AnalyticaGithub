package com.vikas.model.timeseries;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ContributionTimeSeries {
    private final List<TimeSeriesDataPoint> points;
    private final int totalCount;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    public ContributionTimeSeries(List<TimeSeriesDataPoint> points, int totalCount, 
                                  LocalDate periodStart, LocalDate periodEnd) {
        this.points = points;
        this.totalCount = totalCount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public List<TimeSeriesDataPoint> getPoints() {
        return points;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getPeriodStart() {
        return periodStart.format(DateTimeFormatter.ISO_DATE);
    }

    public String getPeriodEnd() {
        return periodEnd.format(DateTimeFormatter.ISO_DATE);
    }
}