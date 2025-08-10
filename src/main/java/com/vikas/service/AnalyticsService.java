package com.vikas.service;

import com.vikas.model.Contribution;

public interface AnalyticsService {

    Contribution getContributions(String username, String mode);

    Contribution getContributionCalendar(String username, String mode);

}
