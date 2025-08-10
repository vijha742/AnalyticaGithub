package com.vikas.service.impl;
//
//import com.vikas.model.Contribution;
//import com.vikas.model.ReadmeQuality;
//import com.vikas.model.timeseries.ContributionCalendar;
import com.vikas.model.Contribution;
//import com.vikas.model.timeseries.ContributionDay;
//import com.vikas.repository.ContributionsRepository;
import com.vikas.model.SuggestedUser;
import com.vikas.model.User;
import com.vikas.repository.ContributionsRepository;
import com.vikas.repository.SuggestedUserRepository;
import com.vikas.repository.UserRepository;
import com.vikas.service.AnalyticsService;
import com.vikas.utils.GithubGraphQLClient;
import com.vikas.utils.QueryManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;


@RequiredArgsConstructor
@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {
	private final GithubGraphQLClient gitHubClient;
	private final QueryManager queryManager;
	private final ContributionsRepository repository;
    private final SuggestedUserRepository userRepository;
//
//
//	// TODO: Make changes so that user can define the time for which he wants to get the data.
    @Override
    @Transactional
	public Contribution getContributions(String username, String mode) {
			return repository.findByUser_GithubUsernameAndMode(username, mode).orElseGet(() -> {
            try {
                Contribution newContribution = getContributionCalendar(username, mode);
                return repository.save(newContribution);
            } catch (DataIntegrityViolationException e) {
            return repository.findByUser_GithubUsernameAndMode(username, mode)
                    .orElseThrow(() -> new RuntimeException("Failed to create or find contribution"));
        }
        });
	}

    @Transactional
	@Override
	public Contribution getContributionCalendar(String username, String mode) {
		Map<String, String> variables = Map.of("username", username);
		Map<String, Object> response = gitHubClient.executeQuery(queryManager.getContributionCalendar(), variables);

		Contribution calendar = new Contribution();
        SuggestedUser githubUser = userRepository.findByGithubUsername(username).orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        calendar.setUser(githubUser);
		if (response != null && response.containsKey("user")) {
			Map<String, Object> user = (Map<String, Object>) response.get("user");
			Map<String, Object> contributionsCollection = (Map<String, Object>) user.get("contributionsCollection");
			Map<String, Object> contributionCalendarData = (Map<String, Object>) contributionsCollection
					.get("contributionCalendar");

            int pull_requests = (Integer) contributionsCollection.get("totalPullRequestContributions");
            int issues = (Integer) contributionsCollection.get("totalIssueContributions");
            int commits = (Integer) contributionsCollection.get("totalCommitContributions");
            int totalContributions = (Integer) contributionCalendarData.get("totalContributions");
			calendar.setTotalContributions(totalContributions);
            calendar.setIssues(issues);
            calendar.setCommits(commits);
            calendar.setPull_requests(pull_requests);
            calendar.setMode(mode);
            List<Map<String, Object>> weeksData = (List<Map<String, Object>>) contributionCalendarData.get("weeks");
            if(mode.equals("weekly")) {
                Map<LocalDate, Integer> timeseriesData = new HashMap<>();
                for (Map<String, Object> weekData : weeksData) {
                    String weekStartString =  (String) weekData.get("firstDay");
                    LocalDate weekStart = LocalDate.parse(weekStartString);
                    List<Map<String, Object>> daysData = (List<Map<String, Object>>) weekData.get("contributionDays");
                    int weekCommits = 0;
                    for (Map<String, Object> dayData : daysData) {
                        weekCommits += (Integer) dayData.get("contributionCount");
                    }
                    timeseriesData.put(weekStart, weekCommits);
                }
                calendar.setTimeSeriesData(timeseriesData);
                return repository.save(calendar);
            } else if(mode.equals("monthly")) {
                Map<LocalDate, Integer> timeseriesData = new HashMap<>();
                int monthCommits = 0;
                YearMonth currentMonth = null;

                for (Map<String, Object> weekData : weeksData) {
                    List<Map<String, Object>> daysData = (List<Map<String, Object>>) weekData.get("contributionDays");
                    for (Map<String, Object> dayData : daysData) {
                        String dateString = (String) dayData.get("date");
                        LocalDate date = LocalDate.parse(dateString);
                        YearMonth dateMonth = YearMonth.from(date);
                        if(currentMonth == null) {
                            currentMonth = dateMonth;
                        }
                        if(!currentMonth.equals(dateMonth)) {
                            timeseriesData.put(currentMonth.atDay(1), monthCommits);
                            monthCommits = 0;
                            currentMonth = dateMonth;
                        }
                        monthCommits += (Integer) dayData.get("contributionCount");
                    }
                }

                if(currentMonth != null) {
                    timeseriesData.put(currentMonth.atDay(1), monthCommits);
                }

                calendar.setTimeSeriesData(timeseriesData);
                return repository.save(calendar);
            } else if(mode.equals("daily")) {
                Map<LocalDate, Integer> timeseriesData = new HashMap<>();
                for (Map<String, Object> weekData : weeksData) {
                    List<Map<String, Object>> daysData = (List<Map<String, Object>>) weekData.get("contributionDays");
                    for (Map<String, Object> dayData : daysData) {
                        String dateString = (String) dayData.get("date");
                        LocalDate date = LocalDate.parse(dateString);
                        int dayCommits = (Integer) dayData.get("contributionCount");
                        timeseriesData.put(date, dayCommits);
                    }
                }
                calendar.setTimeSeriesData(timeseriesData);
                return repository.save(calendar);
            }
		}
		return calendar;
	}

}
