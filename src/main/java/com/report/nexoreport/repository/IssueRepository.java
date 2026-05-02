package com.report.nexoreport.repository;

import com.report.nexoreport.issue.IssuePriority;
import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IssueRepository extends JpaRepository<Issue, Long> {
    List<Issue> findByCreatedByOrderByCreatedAtDesc(User createdBy);

    long countByCreatedBy(User createdBy);

    long countByCreatedByAndStatus(User createdBy, IssueStatus status);

    List<Issue> findByTargetTypeOrderByCreatedAtDesc(IssueTargetType targetType);

    List<Issue> findByTargetTypeAndTargetRoleOrderByCreatedAtDesc(IssueTargetType targetType, UserRole role);

    List<Issue> findByTargetTypeAndTargetUserIdOrderByCreatedAtDesc(IssueTargetType targetType, Long targetUserId);

    List<Issue> findByCategoryIgnoreCaseOrderByCreatedAtDesc(String category);

    @Query("""
            select i from Issue i
            where i.status = :status
              and (
                i.createdBy.id = :userId
                or i.targetType = com.report.nexoreport.issue.IssueTargetType.ALL
                or (i.targetType = com.report.nexoreport.issue.IssueTargetType.USER and i.targetUserId = :userId)
                or (i.targetType = com.report.nexoreport.issue.IssueTargetType.ROLE and i.targetRole = :role)
                or (i.assignedTo is not null and i.assignedTo.id = :userId)
              )
            order by i.createdAt desc
            """)
    List<Issue> findVisibleByStatusOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("role") UserRole role,
            @Param("status") IssueStatus status
    );

    long countByAssignedToAndStatus(User assignedTo, IssueStatus status);

    @Query("""
            select count(i) from Issue i
            where i.status <> com.report.nexoreport.issue.IssueStatus.RESOLVED
              and i.priority in :priorities
            """)
    long countPriorityUnresolved(@Param("priorities") List<IssuePriority> priorities);

    @Query("""
            select count(i) from Issue i
            where i.status <> com.report.nexoreport.issue.IssueStatus.RESOLVED
              and i.priority in :priorities
              and i.createdAt < :cutoff
            """)
    long countOverduePriorityUnresolved(@Param("priorities") List<IssuePriority> priorities, @Param("cutoff") Instant cutoff);

    @Query("""
            select i from Issue i
            where i.status = com.report.nexoreport.issue.IssueStatus.PENDING
              and i.priority in :priorities
              and i.createdAt < :cutoff
              and i.assignedTo is not null
            """)
    List<Issue> findOverduePriorityPendingAssigned(@Param("priorities") List<IssuePriority> priorities, @Param("cutoff") Instant cutoff);

    interface PendingAssignedCountRow {
        Long getAssignedToId();

        long getPendingCount();
    }

    @Query(
            value = """
                    select i.assigned_to as assignedToId, count(*) as pendingCount
                    from issues i
                    where i.status = 'PENDING'
                      and i.assigned_to is not null
                    group by i.assigned_to
                    having count(*) > :threshold
                    """,
            nativeQuery = true
    )
    List<PendingAssignedCountRow> findAssignedUsersWithPendingCountAbove(@Param("threshold") long threshold);

    long count();

    long countByStatus(IssueStatus status);
}
