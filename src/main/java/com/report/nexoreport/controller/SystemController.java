package com.report.nexoreport.controller;

import com.report.nexoreport.dto.PrioritySummaryResponse;
import com.report.nexoreport.dto.SystemActivityResponse;
import com.report.nexoreport.service.SystemMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
@Tag(name = "System Monitoring", description = "System monitoring and summary endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SystemController {
    private final SystemMonitoringService systemMonitoringService;

    public SystemController(SystemMonitoringService systemMonitoringService) {
        this.systemMonitoringService = systemMonitoringService;
    }

    @GetMapping("/activity")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    @Operation(summary = "System activity", description = "Return number of issues resolved per day (descending by date)")
    public ResponseEntity<List<SystemActivityResponse>> activity() {
        return ResponseEntity.ok(systemMonitoringService.systemActivity());
    }

    @GetMapping("/priority-summary")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','NURSE','ADMINISTRATIVE_STAFF')")
    @Operation(summary = "Priority summary", description = "Return summary statistics about unresolved and overdue priority issues")
    public ResponseEntity<PrioritySummaryResponse> prioritySummary() {
        return ResponseEntity.ok(systemMonitoringService.prioritySummary());
    }
}

