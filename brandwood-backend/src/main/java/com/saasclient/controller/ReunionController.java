package com.saasclient.controller;

import com.saasclient.dto.ReunionRequest;
import com.saasclient.dto.ReunionResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ReunionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionService reunionService;

    @GetMapping("/reunions")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<ReunionResponse> mesReunions(@AuthenticationPrincipal UserPrincipal principal) {
        return reunionService.mesReunions(principal.getId());
    }

    @GetMapping("/projets/{projetId}/reunions")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<ReunionResponse> listerParProjet(@AuthenticationPrincipal UserPrincipal principal,
                                                 @PathVariable Long projetId) {
        return reunionService.listerParProjet(principal.getId(), projetId);
    }

    @PostMapping("/projets/{projetId}/reunions")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReunionResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                                 @PathVariable Long projetId, @Valid @RequestBody ReunionRequest req) {
        return reunionService.creer(principal.getId(), projetId, req);
    }

    @PutMapping("/reunions/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ReunionResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id, @Valid @RequestBody ReunionRequest req) {
        return reunionService.modifier(principal.getId(), id, req);
    }

    @DeleteMapping("/reunions/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        reunionService.supprimer(principal.getId(), id);
    }
}
