package com.saasclient.controller;

import com.saasclient.dto.FactureRequest;
import com.saasclient.dto.FactureResponse;
import com.saasclient.dto.FactureStatutRequest;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<FactureResponse> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return factureService.lister(principal.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public FactureResponse obtenir(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return factureService.obtenir(principal.getId(), id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public FactureResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody FactureRequest req) {
        return factureService.creer(principal.getId(), req);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public FactureResponse changerStatut(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long id, @Valid @RequestBody FactureStatutRequest req) {
        return factureService.changerStatut(principal.getId(), id, req.getStatut());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        factureService.supprimer(principal.getId(), id);
    }
}
