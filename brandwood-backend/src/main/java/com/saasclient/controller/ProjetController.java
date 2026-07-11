package com.saasclient.controller;

import com.saasclient.dto.JalonRequest;
import com.saasclient.dto.JalonResponse;
import com.saasclient.dto.MembreRequest;
import com.saasclient.dto.MembreResponse;
import com.saasclient.dto.ProjetRequest;
import com.saasclient.dto.ProjetResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ProjetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public Page<ProjetResponse> lister(@AuthenticationPrincipal UserPrincipal principal,
                                       @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return projetService.lister(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public ProjetResponse obtenir(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return projetService.obtenir(principal.getId(), id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjetResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody ProjetRequest req) {
        return projetService.creer(principal.getId(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public ProjetResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                   @PathVariable Long id, @Valid @RequestBody ProjetRequest req) {
        return projetService.modifier(principal.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        projetService.supprimer(principal.getId(), id);
    }

    // ── Jalons (sous-ressource) ──
    @GetMapping("/{id}/jalons")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<JalonResponse> jalons(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return projetService.listerJalons(principal.getId(), id);
    }

    @PostMapping("/{id}/jalons")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public JalonResponse ajouterJalon(@AuthenticationPrincipal UserPrincipal principal,
                                      @PathVariable Long id, @Valid @RequestBody JalonRequest req) {
        return projetService.ajouterJalon(principal.getId(), id, req);
    }

    // ── Membres / équipe (sous-ressource) ──
    @GetMapping("/{id}/membres")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<MembreResponse> membres(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return projetService.listerMembres(principal.getId(), id);
    }

    @PostMapping("/{id}/membres")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public MembreResponse ajouterMembre(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id, @Valid @RequestBody MembreRequest req) {
        return projetService.ajouterMembre(principal.getId(), id, req);
    }
}
