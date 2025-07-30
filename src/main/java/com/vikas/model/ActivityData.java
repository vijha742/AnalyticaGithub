package com.vikas.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ActivityData
 */
@Data
public class ActivityData {
    private List<CommitData> commits;
    private IssuesData issues;

}

@Data
class CommitData {
    private LocalDateTime commitDate;
    private String commitMessage;
    private String userName;
    private int additions;
    private int deletions;
}

@Data
class IssuesData {
    private int issuesOpen;
    private int issuesClosed;
}
