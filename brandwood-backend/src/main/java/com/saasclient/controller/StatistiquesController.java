package com.saasclient.controller;

import com.saasclient.dto.InsightsResponse;
import com.saasclient.dto.StatistiquesResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.StatistiquesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistiques")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE','SUPER_ADMIN')")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    @GetMapping
    public StatistiquesResponse charger(@AuthenticationPrincipal UserPrincipal principal) {
        return statistiquesService.charger(principal.getId());
    }

    @GetMapping("/ia/insights")
    public InsightsResponse insights(@AuthenticationPrincipal UserPrincipal principal) {
        return statistiquesService.insights(principal.getId());
    }
}
