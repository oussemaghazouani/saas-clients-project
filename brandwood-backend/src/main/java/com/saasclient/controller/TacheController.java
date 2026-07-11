package com.saasclient.controller;

import com.saasclient.dto.TacheRequest;
import com.saasclient.dto.TacheResponse;
import com.saasclient.dto.TacheStatutRequest;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.TacheService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/taches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class TacheController {

    private final TacheService tacheService;

    @PutMapping("/{id}")
    public TacheResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long id, @Valid @RequestBody TacheRequest req) {
        return tacheService.modifier(principal.getId(), id, req);
    }

    @PatchMapping("/{id}/statut")
    public TacheResponse changerStatut(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id, @Valid @RequestBody TacheStatutRequest req) {
        return tacheService.changerStatut(principal.getId(), id, req.getStatut());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        tacheService.supprimer(principal.getId(), id);
    }
}
