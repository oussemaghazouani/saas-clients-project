package com.saasclient.controller;

import com.saasclient.dto.NotificationResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE','SUPER_ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return notificationService.lister(principal.getId());
    }

    @GetMapping("/count")
    public Map<String, Long> compterNonLues(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("nonLues", notificationService.compterNonLues(principal.getId()));
    }

    @PatchMapping("/{id}/lu")
    public void marquerLu(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        notificationService.marquerLu(principal.getId(), id);
    }

    @PatchMapping("/lu-tout")
    public void marquerToutLu(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.marquerToutLu(principal.getId());
    }
}
