package com.report.nexoreport.service;

import com.report.nexoreport.dto.DashboardResponse;
import com.report.nexoreport.dto.IssueResponseDto;
import com.report.nexoreport.exception.ResourceNotFoundException;
import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.repository.IssueRepository;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private static final Set<UserRole> STUDENT_LIKE_ROLES = Set.of(
            UserRole.CLASS_MONITOR,
            UserRole.COMMITTEE_MEMBER
    );

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    public DashboardService(IssueRepository issueRepository, UserRepository userRepository) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse dashboard(Authentication authentication) {
        User current = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (STUDENT_LIKE_ROLES.contains(current.getRole())) {
            List<Issue> submitted = issueRepository.findByCreatedByOrderByCreatedAtDesc(current);
            long resolved = submitted.stream().filter(issue -> issue.getStatus() == IssueStatus.RESOLVED).count();
            long inProgress = submitted.stream().filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS).count();
            List<IssueResponseDto> recent = submitted.stream().limit(5).map(this::toDto).toList();
            return new DashboardResponse(submitted.size(), 0, resolved, inProgress, 0, Map.of(), recent, List.of());
        }

        List<Issue> received = issueRepository.findAll()
                .stream()
                .filter(issue -> canUserAccessIssue(current, issue) && !issue.getCreatedBy().getId().equals(current.getId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
        long resolved = received.stream().filter(issue -> issue.getStatus() == IssueStatus.RESOLVED).count();
        long inProgress = received.stream().filter(issue -> issue.getStatus() == IssueStatus.IN_PROGRESS).count();
        long pending = received.stream().filter(issue -> issue.getStatus() == IssueStatus.PENDING).count();

        Map<String, Long> issuesByCategory = new LinkedHashMap<>();
        for (Issue issue : received) {
            issuesByCategory.put(issue.getCategory(), issuesByCategory.getOrDefault(issue.getCategory(), 0L) + 1);
        }
        List<IssueResponseDto> recent = received.stream().limit(5).map(this::toDto).toList();
        return new DashboardResponse(0, received.size(), resolved, inProgress, pending, issuesByCategory, List.of(), recent);
    }

    private boolean canUserAccessIssue(User user, Issue issue) {
        if (issue.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        if (issue.getTargetType() == IssueTargetType.ALL) {
            return true;
        }
        if (issue.getTargetType() == IssueTargetType.USER) {
            return issue.getTargetUserId() != null && issue.getTargetUserId().equals(user.getId());
        }
        return issue.getTargetType() == IssueTargetType.ROLE && issue.getTargetRole() == user.getRole();
    }

    private IssueResponseDto toDto(Issue issue) {
        return new IssueResponseDto(
                issue.getId(),
                issue.getTitle(),
                issue.getDescription(),
                issue.getCategory(),
                issue.getPriority(),
                issue.getStatus(),
                issue.getCreatedAt(),
                issue.getCreatedBy().getId(),
                issue.getCreatedBy().getEmail(),
                issue.getTargetType(),
                issue.getTargetUserId(),
                issue.getTargetRole()
        );
    }
}
