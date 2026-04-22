package com.report.nexoreport.controller;

import com.report.nexoreport.dto.MessageResponse;
import com.report.nexoreport.dto.NotificationResponseDto;
import com.report.nexoreport.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification retrieval and read management")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/my")
    @Operation(summary = "Get my notifications", description = "Get current user's notifications ordered by latest first")
    public ResponseEntity<List<NotificationResponseDto>> myNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.myNotifications(authentication));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public ResponseEntity<MessageResponse> markAsRead(Authentication authentication, @PathVariable Long id) {
        notificationService.markAsRead(authentication, id);
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<MessageResponse> markAllAsRead(Authentication authentication) {
        notificationService.markAllAsRead(authentication);
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }
}
