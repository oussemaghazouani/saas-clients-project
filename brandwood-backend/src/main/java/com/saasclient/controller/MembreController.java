package com.saasclient.controller;

import com.saasclient.dto.MembreRequest;
import com.saasclient.dto.MembreResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ProjetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class MembreController {

    private final ProjetService projetService;

    @PutMapping("/{id}")
    public MembreResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long id, @Valid @RequestBody MembreRequest req) {
        return projetService.modifierMembre(principal.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        projetService.supprimerMembre(principal.getId(), id);
    }
}
