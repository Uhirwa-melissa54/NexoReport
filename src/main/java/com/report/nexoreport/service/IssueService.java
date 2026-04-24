package com.report.nexoreport.service;

import com.report.nexoreport.comment.Comment;
import com.report.nexoreport.dto.CommentResponseDto;
import com.report.nexoreport.dto.AssignIssueRequest;
import com.report.nexoreport.dto.IssueActivityResponseDto;
import com.report.nexoreport.dto.IssueCommentRequest;
import com.report.nexoreport.dto.IssueCreateRequest;
import com.report.nexoreport.dto.IssueDashboardResponse;
import com.report.nexoreport.dto.IssueEditRequest;
import com.report.nexoreport.dto.IssueResolveRequest;
import com.report.nexoreport.dto.IssueResponseDto;
import com.report.nexoreport.dto.IssueThreadResponse;
import com.report.nexoreport.exception.BadRequestException;
import com.report.nexoreport.exception.ResourceNotFoundException;
import com.report.nexoreport.issue.IssuePriority;
import com.report.nexoreport.issue.IssueResponseType;
import com.report.nexoreport.issue.IssueStatus;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.model.Issue;
import com.report.nexoreport.model.IssueResponse;
import com.report.nexoreport.notification.NotificationType;
import com.report.nexoreport.repository.CommentRepository;
import com.report.nexoreport.repository.IssueRepository;
import com.report.nexoreport.repository.IssueResponseRepository;
import com.report.nexoreport.repository.UserRepository;
import com.report.nexoreport.user.User;
import com.report.nexoreport.user.UserRole;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IssueService {
    private static final Logger log = LoggerFactory.getLogger(IssueService.class);
    private static final Set<UserRole> STAFF_ROLES = Set.of(
            UserRole.ADMIN, UserRole.TEACHER, UserRole.NURSE, UserRole.ADMINISTRATIVE_STAFF
    );
    private static final Set<UserRole> STUDENT_LIKE_ROLES = Set.of(
            UserRole.CLASS_MONITOR, UserRole.COMMITTEE_MEMBER
    );

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final IssueResponseRepository issueResponseRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public IssueService(
            IssueRepository issueRepository,
            UserRepository userRepository,
            IssueResponseRepository issueResponseRepository,
            CommentRepository commentRepository,
            NotificationService notificationService
    ) {
        this.issueRepository = issueRepository;
        this.userRepository = userRepository;
        this.issueResponseRepository = issueResponseRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public IssueResponseDto createIssue(Authentication auth, IssueCreateRequest request) {
        User sender = getCurrentUser(auth);
        validateTargetRules(sender, request);

        Issue issue = new Issue();
        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setCategory(request.getCategory());
        issue.setPriority(request.getPriority());
        issue.setStatus(IssueStatus.PENDING);
        issue.setCreatedBy(sender);
        issue.setTargetType(request.getTargetType());
        issue.setTargetUserId(request.getTargetUserId());
        issue.setTargetRole(request.getTargetRole());
        Issue saved = issueRepository.save(issue);

        List<User> receivers = resolveRecipients(saved);
        notificationService.notifyUsers(
                receivers,
                "New issue: " + saved.getTitle(),
                NotificationType.ISSUE_CREATED
        );
        return toDto(saved);
    }

    public IssueDashboardResponse myIssues(Authentication auth) {
        User current = getCurrentUser(auth);
        List<Issue> issues = issueRepository.findByCreatedByOrderByCreatedAtDesc(current);
        long total = issueRepository.countByCreatedBy(current);
        long pending = issueRepository.countByCreatedByAndStatus(current, IssueStatus.PENDING);
        long inProgress = issueRepository.countByCreatedByAndStatus(current, IssueStatus.IN_PROGRESS);
        long resolved = issueRepository.countByCreatedByAndStatus(current, IssueStatus.RESOLVED);
        return new IssueDashboardResponse(total, pending, inProgress, resolved, issues.stream().map(this::toDto).toList());
    }

    public IssueDashboardResponse receivedIssues(Authentication auth) {
        User current = getCurrentUser(auth);
        List<Issue> received = getIssuesVisibleToUser(current);
        long pending = received.stream().filter(i -> i.getStatus() == IssueStatus.PENDING).count();
        long inProgress = received.stream().filter(i -> i.getStatus() == IssueStatus.IN_PROGRESS).count();
        long resolved = received.stream().filter(i -> i.getStatus() == IssueStatus.RESOLVED).count();
        return new IssueDashboardResponse(received.size(), pending, inProgress, resolved, received.stream().map(this::toDto).toList());
    }

    public List<IssueResponseDto> issuesByCategory(Authentication auth, String category) {
        User current = getCurrentUser(auth);
        return issueRepository.findByCategoryIgnoreCaseOrderByCreatedAtDesc(category)
                .stream()
                .filter(issue -> canUserAccessIssue(current, issue))
                .map(this::toDto)
                .toList();
    }

    public List<IssueResponseDto> broadcastFeed() {
        return issueRepository.findByTargetTypeOrderByCreatedAtDesc(IssueTargetType.ALL)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public IssueThreadResponse issueThread(Authentication auth, Long issueId) {
        User current = getCurrentUser(auth);
        Issue issue = getIssueOrThrow(issueId);
        if (!canUserAccessIssue(current, issue)) {
            throw new BadRequestException("You cannot access this issue");
        }

        List<CommentResponseDto> comments = commentRepository.findByIssueOrderByCreatedAtAsc(issue)
                .stream()
                .filter(comment -> canViewComment(current, issue, comment))
                .map(this::toCommentDto)
                .toList();

        List<IssueActivityResponseDto> activities = issueResponseRepository.findByIssueOrderByCreatedAtAsc(issue)
                .stream()
                .map(this::toActivityDto)
                .toList();

        return new IssueThreadResponse(toDto(issue), comments, activities);
    }

    @Transactional
    public IssueResponseDto editIssue(Authentication auth, Long issueId, IssueEditRequest request) {
        User current = getCurrentUser(auth);
        Issue issue = getIssueOrThrow(issueId);
        if (!issue.getCreatedBy().getId().equals(current.getId())) {
            throw new BadRequestException("Only creator can edit issue");
        }
        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new BadRequestException("Resolved issue cannot be edited");
        }

        issue.setTitle(request.getTitle());
        issue.setDescription(request.getDescription());
        issue.setCategory(request.getCategory());
        issue.setPriority(request.getPriority());
        Issue saved = issueRepository.save(issue);

        notificationService.notifyUsers(
                resolveRecipients(saved),
                "Issue updated: " + saved.getTitle(),
                NotificationType.ISSUE_UPDATED
        );
        return toDto(saved);
    }

    @Transactional
    public void commentIssue(Authentication auth, Long issueId, IssueCommentRequest request) {
        User current = getCurrentUser(auth);
        Issue issue = getIssueOrThrow(issueId);
        enforceCommentPermission(current, issue, request.isPrivateToCreator());

        if (issue.getStatus() == IssueStatus.PENDING) {
            issue.setStatus(IssueStatus.IN_PROGRESS);
            issueRepository.save(issue);
        }

        Comment comment = new Comment();
        comment.setIssue(issue);
        comment.setCreatedBy(current);
        comment.setMessage(request.getMessage());
        comment.setPrivateToCreator(request.isPrivateToCreator());
        commentRepository.save(comment);

        IssueResponse response = new IssueResponse();
        response.setIssue(issue);
        response.setRespondedBy(current);
        response.setMessage(request.getMessage());
        response.setType(IssueResponseType.COMMENT);
        issueResponseRepository.save(response);

        Set<User> notified = new HashSet<>();
        if (request.isPrivateToCreator()) {
            notified.add(issue.getCreatedBy());
            notified.add(current);
        } else {
            notified.addAll(resolveRecipients(issue));
            notified.add(issue.getCreatedBy());
        }
        notificationService.notifyUsers(
                notified,
                "New comment on issue: " + issue.getTitle(),
                NotificationType.ISSUE_COMMENTED
        );
    }

    @Transactional
    public void resolveIssue(Authentication auth, Long issueId, IssueResolveRequest request) {
        User current = getCurrentUser(auth);
        Issue issue = getIssueOrThrow(issueId);
        if (!isStaff(current.getRole())) {
            throw new BadRequestException("Only staff users can resolve issues");
        }
        if (!canUserAccessIssue(current, issue)) {
            throw new BadRequestException("You cannot resolve this issue");
        }
        if (issue.getStatus() == IssueStatus.RESOLVED) {
            throw new BadRequestException("Issue already resolved");
        }
        issue.setStatus(IssueStatus.RESOLVED);
        issueRepository.save(issue);

        IssueResponse response = new IssueResponse();
        response.setIssue(issue);
        response.setRespondedBy(current);
        response.setMessage(request.getResolutionMessage());
        response.setType(IssueResponseType.RESOLVED);
        issueResponseRepository.save(response);

        notificationService.notifyUsers(
                List.of(issue.getCreatedBy()),
                "Issue resolved: " + issue.getTitle(),
                NotificationType.ISSUE_RESOLVED
        );
    }

    @Transactional
    public IssueResponseDto resendIssue(Authentication auth, Long issueId) {
        User current = getCurrentUser(auth);
        Issue source = getIssueOrThrow(issueId);
        if (!source.getCreatedBy().getId().equals(current.getId())) {
            throw new BadRequestException("Only creator can resend issue");
        }

        Issue duplicate = new Issue();
        duplicate.setTitle(source.getTitle());
        duplicate.setDescription(source.getDescription());
        duplicate.setCategory(source.getCategory());
        duplicate.setPriority(source.getPriority());
        duplicate.setStatus(IssueStatus.PENDING);
        duplicate.setCreatedBy(current);
        duplicate.setTargetType(source.getTargetType());
        duplicate.setTargetUserId(source.getTargetUserId());
        duplicate.setTargetRole(source.getTargetRole());
        Issue saved = issueRepository.save(duplicate);

        notificationService.notifyUsers(
                resolveRecipients(saved),
                "Issue resent: " + saved.getTitle(),
                NotificationType.ISSUE_CREATED
        );
        return toDto(saved);
    }

    public List<IssueResponseDto> resolvedIssues(Authentication auth) {
        User current = getCurrentUser(auth);
        return issueRepository.findVisibleByStatusOrderByCreatedAtDesc(current.getId(), current.getRole(), IssueStatus.RESOLVED)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<IssueResponseDto> pendingIssues(Authentication auth) {
        User current = getCurrentUser(auth);
        return issueRepository.findVisibleByStatusOrderByCreatedAtDesc(current.getId(), current.getRole(), IssueStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public long priorityUnresolvedCount() {
        return issueRepository.countPriorityUnresolved(List.of(IssuePriority.HIGH, IssuePriority.CRITICAL));
    }

    @Transactional
    public void assignIssue(Authentication auth, Long issueId, AssignIssueRequest request) {
        User admin = getCurrentUser(auth);
        if (admin.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Only admin can assign issues");
        }

        Issue issue = getIssueOrThrow(issueId);
        User assigned = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Assigned user not found"));

        Set<UserRole> assignableRoles = Set.of(
                UserRole.ADMIN,
                UserRole.TEACHER,
                UserRole.NURSE,
                UserRole.ADMINISTRATIVE_STAFF,
                UserRole.COMMITTEE_MEMBER,
                UserRole.CLASS_MONITOR
        );
        if (!assignableRoles.contains(assigned.getRole())) {
            throw new BadRequestException("Assigned user must be a staff, committee member, or monitor");
        }

        issue.setAssignedTo(assigned);
        issue.setStatus(IssueStatus.IN_PROGRESS);
        issueRepository.save(issue);

        notificationService.notifyUsers(
                List.of(assigned),
                "You have been assigned to issue: " + issue.getTitle() + " (Issue #" + issue.getId() + ")",
                NotificationType.ISSUE_ASSIGNED
        );
        log.info("Issue {} assigned to user {}", issueId, request.getAssignedUserId());
    }

    private void validateTargetRules(User sender, IssueCreateRequest request) {
        if (request.getTargetType() == IssueTargetType.USER && request.getTargetUserId() == null) {
            throw new BadRequestException("targetUserId is required for USER target type");
        }
        if (request.getTargetType() == IssueTargetType.ROLE && request.getTargetRole() == null) {
            throw new BadRequestException("targetRole is required for ROLE target type");
        }

        if (STUDENT_LIKE_ROLES.contains(sender.getRole())) {
            if (request.getTargetType() == IssueTargetType.ALL) {
                throw new BadRequestException("Student-like roles cannot broadcast to ALL");
            }
            if (request.getTargetType() == IssueTargetType.ROLE && !isStaff(request.getTargetRole())) {
                throw new BadRequestException("Student-like roles can send only to staff roles");
            }
            if (request.getTargetType() == IssueTargetType.USER) {
                User target = userRepository.findById(request.getTargetUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));
                if (!isStaff(target.getRole())) {
                    throw new BadRequestException("Student-like roles can send only to staff users");
                }
            }
        }
    }

    private void enforceCommentPermission(User user, Issue issue, boolean privateToCreator) {
        boolean canAccess = canUserAccessIssue(user, issue);
        if (!canAccess) {
            throw new BadRequestException("You cannot comment on this issue");
        }
        if (privateToCreator && !isBroadcast(issue)) {
            throw new BadRequestException("Private to creator comments are only for broadcast or role-targeted issues");
        }
    }

    private List<Issue> getIssuesVisibleToUser(User user) {
        List<Issue> all = issueRepository.findAll();
        return all.stream()
                .filter(issue -> canUserAccessIssue(user, issue) && !issue.getCreatedBy().getId().equals(user.getId()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
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

    private List<User> resolveRecipients(Issue issue) {
        if (issue.getTargetType() == IssueTargetType.ALL) {
            return userRepository.findAll();
        }
        if (issue.getTargetType() == IssueTargetType.USER && issue.getTargetUserId() != null) {
            return List.of(userRepository.findById(issue.getTargetUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target user not found")));
        }
        if (issue.getTargetType() == IssueTargetType.ROLE && issue.getTargetRole() != null) {
            List<User> users = new ArrayList<>();
            for (User user : userRepository.findAll()) {
                if (user.getRole() == issue.getTargetRole()) {
                    users.add(user);
                }
            }
            return users;
        }
        return List.of();
    }

    private boolean isStaff(UserRole role) {
        return STAFF_ROLES.contains(role);
    }

    private boolean isBroadcast(Issue issue) {
        return issue.getTargetType() == IssueTargetType.ALL || issue.getTargetType() == IssueTargetType.ROLE;
    }

    private boolean canViewComment(User user, Issue issue, Comment comment) {
        if (!comment.isPrivateToCreator()) {
            return true;
        }
        return user.getId().equals(issue.getCreatedBy().getId()) || user.getId().equals(comment.getCreatedBy().getId());
    }

    private Issue getIssueOrThrow(Long issueId) {
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found"));
    }

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
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

    private CommentResponseDto toCommentDto(Comment comment) {
        return new CommentResponseDto(
                comment.getId(),
                comment.getIssue().getId(),
                comment.getMessage(),
                comment.getCreatedBy().getId(),
                comment.getCreatedBy().getEmail(),
                comment.isPrivateToCreator(),
                comment.getCreatedAt()
        );
    }

    private IssueActivityResponseDto toActivityDto(IssueResponse issueResponse) {
        return new IssueActivityResponseDto(
                issueResponse.getId(),
                issueResponse.getIssue().getId(),
                issueResponse.getMessage(),
                issueResponse.getRespondedBy().getId(),
                issueResponse.getRespondedBy().getEmail(),
                issueResponse.getType(),
                issueResponse.getCreatedAt()
        );
    }
}
