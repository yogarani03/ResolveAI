package com.resolveai.controller;

import com.resolveai.dto.DashboardResponse;
import com.resolveai.entity.User;
import com.resolveai.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/admin")
    public ResponseEntity<DashboardResponse> admin() {
        return ResponseEntity.ok(dashboardService.buildAdminDashboard());
    }

    @GetMapping("/staff")
    public ResponseEntity<DashboardResponse> staff(Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(dashboardService.buildStaffDashboard(user));
    }

    @GetMapping("/user")
    public ResponseEntity<DashboardResponse> user(Authentication auth) {
        User user = currentUserProvider.get(auth);
        return ResponseEntity.ok(dashboardService.buildUserDashboard(user));
    }
}
