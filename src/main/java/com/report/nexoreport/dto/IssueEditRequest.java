package com.report.nexoreport.dto;

import com.report.nexoreport.issue.IssuePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IssueEditRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String category;
    @NotNull
    private IssuePriority priority;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public IssuePriority getPriority() { return priority; }
    public void setPriority(IssuePriority priority) { this.priority = priority; }
}
