package com.report.nexoreport.service;

import com.report.nexoreport.dto.PrioritySummaryResponse;
import com.report.nexoreport.dto.SystemActivityResponse;
import com.report.nexoreport.issue.IssuePriority;
import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.notification.NotificationType;
import com.report.nexoreport.repository.IssueRepository;
import com.report.nexoreport.repository.IssueResponseRepository;
import com.report.nexoreport.repository.NotificationRepository;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.user.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SystemMonitoringService {
    private static final Logger log = LoggerFactory.getLogger(SystemMonitoringService.class);
    private static final List<IssuePriority> PRIORITY_LEVELS = List.of(IssuePriority.HIGH, IssuePriority.CRITICAL);

    private final IssueRepository issueRepository;
    private final IssueResponseRepository issueResponseRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public SystemMonitoringService(
            IssueRepository issueRepository,
            IssueResponseRepository issueResponseRepository,
            NotificationService notificationService,
            NotificationRepository notificationRepository,
            UserRepository userRepository
    ) {
        this.issueRepository = issueRepository;
        this.issueResponseRepository = issueResponseRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<SystemActivityResponse> systemActivity() {
        return issueResponseRepository.getResolvedIssuesPerDay()
                .stream()
                .map(row -> new SystemActivityResponse(row.getDate(), row.getResolvedCount()))
                .toList();
    }

    public PrioritySummaryResponse prioritySummary() {
        long priorityUnresolvedTotal = issueRepository.countPriorityUnresolved(PRIORITY_LEVELS);
        long overduePriorityTotal = issueRepository.countOverduePriorityUnresolved(PRIORITY_LEVELS, Instant.now().minus(2, ChronoUnit.DAYS));
        return new PrioritySummaryResponse(priorityUnresolvedTotal, overduePriorityTotal);
    }

    public long priorityUnresolvedCount() {
        return issueRepository.countPriorityUnresolved(PRIORITY_LEVELS);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void runAlertsHourly() {
        triggerOverduePriorityPendingAlerts();
        triggerPendingThresholdAlerts();
    }

    private void triggerOverduePriorityPendingAlerts() {
        Instant cutoff = Instant.now().minus(2, ChronoUnit.DAYS);
        List<Issue> overdue = issueRepository.findOverduePriorityPendingAssigned(PRIORITY_LEVELS, cutoff);
        for (Issue issue : overdue) {
            User assigned = issue.getAssignedTo();
            if (assigned == null) {
                continue;
            }
            String message = "Priority issue overdue (>2 days) and still pending: " + issue.getTitle() + " (Issue #" + issue.getId() + ")";
            boolean alreadyNotified = notificationRepository.existsByUserAndTypeAndMessageAndCreatedAtAfter(
                    assigned,
                    NotificationType.PRIORITY_OVERDUE,
                    message,
                    Instant.now().minus(24, ChronoUnit.HOURS)
            );
            if (alreadyNotified) {
                continue;
            }
            notificationService.notifyUsers(List.of(assigned), message, NotificationType.PRIORITY_OVERDUE);
            log.info("Priority alert triggered for issue {} assigned to user {}", issue.getId(), assigned.getId());
        }
    }

    private void triggerPendingThresholdAlerts() {
        List<IssueRepository.PendingAssignedCountRow> rows = issueRepository.findAssignedUsersWithPendingCountAbove(10);
        if (rows.isEmpty()) {
            return;
        }

        Collection<Long> userIds = rows.stream()
                .map(IssueRepository.PendingAssignedCountRow::getAssignedToId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, User> usersById = userRepository.findAllById(userIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, Function.identity()));

        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        for (IssueRepository.PendingAssignedCountRow row : rows) {
            User user = usersById.get(row.getAssignedToId());
            if (user == null) {
                continue;
            }
            String message = "You have " + row.getPendingCount() + " pending issues assigned to you (threshold exceeded)";
            boolean alreadyNotified = notificationRepository.existsByUserAndTypeAndMessageAndCreatedAtAfter(
                    user,
                    NotificationType.PENDING_THRESHOLD_EXCEEDED,
                    message,
                    since
            );
            if (alreadyNotified) {
                continue;
            }
            notificationService.notifyUsers(List.of(user), message, NotificationType.PENDING_THRESHOLD_EXCEEDED);
            log.info("Pending threshold exceeded for user {} with {}", user.getId(), row.getPendingCount());
        }
    }
}

