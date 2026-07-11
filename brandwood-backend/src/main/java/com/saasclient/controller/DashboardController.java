package com.saasclient.controller;

import com.saasclient.dto.DashboardResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE','SUPER_ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse charger(@AuthenticationPrincipal UserPrincipal principal) {
        return dashboardService.charger(principal.getId());
    }
}
