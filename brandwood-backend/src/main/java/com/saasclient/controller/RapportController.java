package com.saasclient.controller;

import com.saasclient.dto.RapportProjetResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projets/{projetId}/rapport")
@RequiredArgsConstructor
public class RapportController {

    private final RapportService rapportService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public RapportProjetResponse rapport(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long projetId) {
        return rapportService.genererRapportProjet(principal.getId(), projetId);
    }
}
