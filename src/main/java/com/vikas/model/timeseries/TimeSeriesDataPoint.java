package com.vikas.model.timeseries;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TimeSeriesDataPoint {
    private final LocalDate date;
    private final int count;

    public TimeSeriesDataPoint(LocalDate date, int count) {
        this.date = date;
        this.count = count;
    }

    public String getDate() {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public int getCount() {
        return count;
    }
}