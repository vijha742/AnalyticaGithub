package com.vikas.service;

import com.vikas.model.timeseries.ContributionCalendar;


public interface AnalyticsService {

    ContributionCalendar getContributionCalendar(String username);
}
