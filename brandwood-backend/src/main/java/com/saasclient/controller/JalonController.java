package com.saasclient.controller;

import com.saasclient.dto.JalonRequest;
import com.saasclient.dto.JalonResponse;
import com.saasclient.dto.JalonStatutRequest;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ProjetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jalons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class JalonController {

    private final ProjetService projetService;

    @PutMapping("/{id}")
    public JalonResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long id, @Valid @RequestBody JalonRequest req) {
        return projetService.modifierJalon(principal.getId(), id, req);
    }

    @PatchMapping("/{id}/statut")
    public JalonResponse changerStatut(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long id, @Valid @RequestBody JalonStatutRequest req) {
        return projetService.changerStatutJalon(principal.getId(), id, req.getStatut());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        projetService.supprimerJalon(principal.getId(), id);
    }
}
