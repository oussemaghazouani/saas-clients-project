package com.saasclient.controller;

import com.saasclient.dto.AnalyseRisqueResponse;
import com.saasclient.dto.GenerationTachesResponse;
import com.saasclient.dto.TacheRequest;
import com.saasclient.dto.TacheResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.TacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets/{projetId}")
@RequiredArgsConstructor
public class ProjetTacheController {

    private final TacheService tacheService;

    @GetMapping("/taches")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<TacheResponse> lister(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long projetId) {
        return tacheService.lister(principal.getId(), projetId);
    }

    @PostMapping("/taches")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public TacheResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                               @PathVariable Long projetId, @Valid @RequestBody TacheRequest req) {
        return tacheService.creer(principal.getId(), projetId, req);
    }

    /** IA : génération intelligente du découpage de tâches. */
    @PostMapping("/taches/generer")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public GenerationTachesResponse generer(@AuthenticationPrincipal UserPrincipal principal,
                                            @PathVariable Long projetId) {
        return tacheService.genererTaches(principal.getId(), projetId);
    }

    /** IA : analyse du risque de retard. */
    @GetMapping("/risque")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public AnalyseRisqueResponse risque(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long projetId) {
        return tacheService.analyserRisque(principal.getId(), projetId);
    }
}
