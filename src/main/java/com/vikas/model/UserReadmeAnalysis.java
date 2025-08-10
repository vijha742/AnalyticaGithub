package com.vikas.model;

import lombok.Data;

@Data
public class UserReadmeAnalysis {
    private float userRatings;
    private int percentile100th;
    private int percentile70th;
    private int percentile50th;
    private int percentile30th;
    private int percentile10th;
}
