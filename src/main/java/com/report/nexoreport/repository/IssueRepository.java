package com.report.nexoreport.repository;

import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    long countByCreatedBy(User createdBy);

    long countByCreatedByAndStatus(User createdBy, IssueStatus status);

    List<Issue> findByTargetTypeOrderByCreatedAtDesc(com.report.nexoreport.issue.IssueTargetType targetType);

    List<Issue> findByTargetTypeAndTargetRoleOrderByCreatedAtDesc(com.report.nexoreport.issue.IssueTargetType targetType, UserRole role);

    List<Issue> findByTargetTypeAndTargetUserIdOrderByCreatedAtDesc(com.report.nexoreport.issue.IssueTargetType targetType, Long targetUserId);
}
