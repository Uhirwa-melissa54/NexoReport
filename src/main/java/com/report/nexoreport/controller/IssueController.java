package com.report.nexoreport.controller;

import com.report.nexoreport.dto.AssignIssueRequest;
import com.report.nexoreport.dto.IssueCommentRequest;
import com.report.nexoreport.dto.IssueCreateRequest;
import com.report.nexoreport.dto.IssueDashboardResponse;
import com.report.nexoreport.dto.IssueEditRequest;
import com.report.nexoreport.dto.IssueResolveRequest;
import com.report.nexoreport.dto.IssueResponseDto;
import com.report.nexoreport.dto.IssueThreadResponse;
import com.report.nexoreport.dto.MessageResponse;
import com.report.nexoreport.dto.PriorityUnresolvedCountResponse;
import com.report.nexoreport.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues", description = "Issue reporting and communication endpoints")
@SecurityRequirement(name = "bearerAuth")
public class IssueController {
    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create issue", description = "Create a new issue targeting a user, role, or all users")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Issue created"),
            @ApiResponse(responseCode = "400", description = "Validation or business rule error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<IssueResponseDto> createIssue(Authentication authentication, @Valid @RequestBody IssueCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.createIssue(authentication, request));
    }

    @GetMapping("/my")
    @Operation(summary = "My issues dashboard", description = "Get sender dashboard statistics and list of issues created by current user")
    public ResponseEntity<IssueDashboardResponse> myIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.myIssues(authentication));
    }

    @GetMapping("/received")
    @Operation(summary = "Received issues dashboard", description = "Get receiver dashboard statistics and issues visible to current user")
    public ResponseEntity<IssueDashboardResponse> receivedIssues(Authentication authentication) {
        return ResponseEntity.ok(issueService.receivedIssues(authentication));
    }

    @GetMapping("/{id}/thread")
    @Operation(summary = "Get issue thread", description = "Get issue details, comments, and activity history with visibility filtering")
    public ResponseEntity<IssueThreadResponse> issueThread(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(issueService.issueThread(authentication, id));
    }

    @PutMapping("/{id}/edit")
    @Operation(summary = "Edit issue", description = "Edit issue fields; only creator can edit unresolved issues")
    public ResponseEntity<IssueResponseDto> editIssue(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody IssueEditRequest request
    ) {
        return ResponseEntity.ok(issueService.editIssue(authentication, id, request));
    }

    @PostMapping("/{id}/comment")
    @Operation(summary = "Comment on issue", description = "Add a comment to issue; comment sets status to IN_PROGRESS if currently PENDING")
    public ResponseEntity<MessageResponse> comment(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody IssueCommentRequest request
    ) {
        issueService.commentIssue(authentication, id, request);
        return ResponseEntity.ok(new MessageResponse("Comment added"));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve issue", description = "Resolve issue with resolution message; staff roles only")
    public ResponseEntity<MessageResponse> resolve(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody IssueResolveRequest request
    ) {
        issueService.resolveIssue(authentication, id, request);
        return ResponseEntity.ok(new MessageResponse("Issue resolved"));
    }

    @PostMapping("/{id}/resend")
    @Operation(summary = "Resend issue", description = "Duplicate an issue as a new issue with PENDING status")
    public ResponseEntity<IssueResponseDto> resend(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.resendIssue(authentication, id));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter issues by category", description = "Return issues in a category visible to current user")
    public ResponseEntity<java.util.List<IssueResponseDto>> byCategory(Authentication authentication, @PathVariable String category) {
        return ResponseEntity.ok(issueService.issuesByCategory(authentication, category));
    }

    @GetMapping("/broadcast")
    @Operation(summary = "Broadcast feed", description = "Return latest broadcast issues")
    public ResponseEntity<java.util.List<IssueResponseDto>> broadcastFeed() {
        return ResponseEntity.ok(issueService.broadcastFeed());
    }

    @GetMapping("/resolved")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    @Operation(summary = "Resolved issues", description = "Return resolved issues visible to current user ordered by createdAt desc")
    public ResponseEntity<List<IssueResponseDto>> resolved(Authentication authentication) {
        return ResponseEntity.ok(issueService.resolvedIssues(authentication));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    @Operation(summary = "Pending issues", description = "Return pending issues visible to current user ordered by createdAt desc")
    public ResponseEntity<List<IssueResponseDto>> pending(Authentication authentication) {
        return ResponseEntity.ok(issueService.pendingIssues(authentication));
    }

    @GetMapping("/priority/unresolved/count")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    @Operation(summary = "Priority unresolved total", description = "Count HIGH/CRITICAL priority issues that are not resolved")
    public ResponseEntity<PriorityUnresolvedCountResponse> priorityUnresolvedCount() {
        return ResponseEntity.ok(new PriorityUnresolvedCountResponse(issueService.priorityUnresolvedCount()));
    }

    @PostMapping("/{issueId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign issue", description = "Admin assigns a staff member to handle an issue")
    public ResponseEntity<MessageResponse> assign(
            Authentication authentication,
            @PathVariable Long issueId,
            @Valid @RequestBody AssignIssueRequest request
    ) {
        issueService.assignIssue(authentication, issueId, request);
        return ResponseEntity.ok(new MessageResponse("Issue assigned"));
    }
}
