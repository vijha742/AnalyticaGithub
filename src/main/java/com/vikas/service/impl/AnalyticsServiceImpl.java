package com.vikas.service.impl;

import com.vikas.model.timeseries.ContributionCalendar;
import com.vikas.model.timeseries.ContributionWeek;
import com.vikas.model.timeseries.ContributionDay;
import com.vikas.service.AnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {
    private final GithubGraphQLClient gitHubClient;
    private final QueryManager queryManager;

    public AnalyticsServiceImpl(GithubGraphQLClient gitHubClient) {
        this.queryManager = new QueryManager();
        this.gitHubClient = gitHubClient;
    }

    @Override
    public ContributionCalendar getContributionCalendar(String username) {
        Map<String, String> variables = Map.of("username", username);
        Map<String, Object> response = gitHubClient.executeQuery(queryManager.getContributionCalendar(), variables);

        ContributionCalendar calendar = new ContributionCalendar();

        if (response != null && response.containsKey("user")) {
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            Map<String, Object> contributionsCollection = (Map<String, Object>) user.get("contributionsCollection");
            Map<String, Object> contributionCalendarData = (Map<String, Object>) contributionsCollection.get("contributionCalendar");

            int totalContributions = (Integer) contributionCalendarData.get("totalContributions");
            calendar.setTotalContributions(totalContributions);

            List<Map<String, Object>> weeksData = (List<Map<String, Object>>) contributionCalendarData.get("weeks");
            List<ContributionWeek> weeks = new ArrayList<>();

            for (Map<String, Object> weekData : weeksData) {
                ContributionWeek week = new ContributionWeek();
                week.setFirstDay((String) weekData.get("firstDay"));

                List<Map<String, Object>> daysData = (List<Map<String, Object>>) weekData.get("contributionDays");
                List<ContributionDay> days = new ArrayList<>();

                for (Map<String, Object> dayData : daysData) {
                    ContributionDay day = new ContributionDay();
                    day.setDate((String) dayData.get("date"));
                    day.setContributionCount((Integer) dayData.get("contributionCount"));

                    String level = (String) dayData.get("contributionLevel");
                    days.add(day);
                }
                week.setContributionDays(days);
                weeks.add(week);
            }
            calendar.setWeeks(weeks);
        }
        return calendar;
    }


}
