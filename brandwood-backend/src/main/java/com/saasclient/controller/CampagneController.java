package com.saasclient.controller;

import com.saasclient.dto.CampagneIaRequest;
import com.saasclient.dto.CampagneIaResponse;
import com.saasclient.dto.CampagneRequest;
import com.saasclient.dto.CampagneResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.CampagneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/campagnes")
@RequiredArgsConstructor
public class CampagneController {

    private final CampagneService campagneService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public Page<CampagneResponse> lister(@AuthenticationPrincipal UserPrincipal principal,
                                         @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return campagneService.lister(principal.getId(), pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public CampagneResponse obtenir(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return campagneService.obtenir(principal.getId(), id);
    }

    @PostMapping
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public CampagneResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                                  @Valid @RequestBody CampagneRequest req) {
        return campagneService.creer(principal.getId(), req);
    }

    @PostMapping("/ia/generer")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public CampagneIaResponse genererParIa(@AuthenticationPrincipal UserPrincipal principal,
                                           @Valid @RequestBody CampagneIaRequest req) {
        return campagneService.genererContenu(principal.getId(), req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public CampagneResponse modifier(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long id, @Valid @RequestBody CampagneRequest req) {
        return campagneService.modifier(principal.getId(), id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        campagneService.supprimer(principal.getId(), id);
    }
}
