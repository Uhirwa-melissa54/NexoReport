package com.report.nexoreport.controller;

import com.report.nexoreport.dto.ChangePasswordRequest;
import com.report.nexoreport.dto.InviteUserRequest;
import com.report.nexoreport.dto.MessageResponse;
import com.report.nexoreport.dto.UserResponse;
import com.report.nexoreport.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    public ResponseEntity<UserResponse> inviteUser(@Valid @RequestBody InviteUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.inviteUser(request));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> updatePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.updatePassword(authentication, request);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully"));
    }

    @PostMapping("/resend-invitation/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    public ResponseEntity<MessageResponse> resendInvitation(@PathVariable Long userId) {
        userService.resendInvitation(userId);
        return ResponseEntity.ok(new MessageResponse("Invitation reminder has been sent to the user."));
    }
}
