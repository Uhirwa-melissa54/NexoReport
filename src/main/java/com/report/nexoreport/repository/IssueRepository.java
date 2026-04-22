package com.report.nexoreport.repository;

import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    long countByCreatedBy(User createdBy);

    long countByCreatedByAndStatus(User createdBy, IssueStatus status);

    List<Issue> findByTargetTypeOrderByCreatedAtDesc(IssueTargetType targetType);

    List<Issue> findByTargetTypeAndTargetRoleOrderByCreatedAtDesc(IssueTargetType targetType, UserRole role);

    List<Issue> findByTargetTypeAndTargetUserIdOrderByCreatedAtDesc(IssueTargetType targetType, Long targetUserId);

    List<Issue> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);
}
