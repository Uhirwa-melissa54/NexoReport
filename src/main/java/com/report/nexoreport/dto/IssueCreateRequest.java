package com.report.nexoreport.dto;

import com.report.nexoreport.issue.IssuePriority;
import com.report.nexoreport.issue.IssueTargetType;
import com.report.nexoreport.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IssueCreateRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String category;
    @NotNull
    private IssuePriority priority;
    @NotNull
    private IssueTargetType targetType;
    private Long targetUserId;
    private UserRole targetRole;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
    public IssueTargetType getTargetType() { return targetType; }
    public void setTargetType(IssueTargetType targetType) { this.targetType = targetType; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public UserRole getTargetRole() { return targetRole; }
    public void setTargetRole(UserRole targetRole) { this.targetRole = targetRole; }
}
