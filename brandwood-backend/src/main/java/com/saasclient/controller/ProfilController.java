package com.saasclient.controller;

import com.saasclient.dto.ProfilResponse;
import com.saasclient.dto.UpdateProfilRequest;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ProfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profil")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
public class ProfilController {

    private final ProfilService profilService;

    @GetMapping
    public ProfilResponse monProfil(@AuthenticationPrincipal UserPrincipal principal) {
        return profilService.monProfil(principal.getId());
    }

    @PutMapping
    public ProfilResponse mettreAJour(@AuthenticationPrincipal UserPrincipal principal,
                                      @Valid @RequestBody UpdateProfilRequest req) {
        return profilService.mettreAJour(principal.getId(), req);
    }
}
