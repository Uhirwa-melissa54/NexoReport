package com.report.nexoreport.dto;

import com.report.nexoreport.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class InviteUserRequest {

    @Email
    @NotBlank
    private String email;



    @NotBlank
    private String fullNames;

    @NotNull
    private UserRole role;

    private String className;

    private String committeePosition;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }
    public String getFullNames() {
        return fullNames;
    }

    public void setFullNames(String fullNames) {
        this.fullNames = fullNames;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getCommitteePosition() {
        return committeePosition;
    }

    public void setCommitteePosition(String committeePosition) {
        this.committeePosition = committeePosition;
    }
}
