package com.saasclient.controller;

import com.saasclient.dto.AbonnementResponse;
import com.saasclient.dto.SouscriptionRequest;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.AbonnementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestataire/abonnements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class PrestataireAbonnementController {

    private final AbonnementService abonnementService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AbonnementResponse souscrire(@AuthenticationPrincipal UserPrincipal principal,
                                        @Valid @RequestBody SouscriptionRequest req) {
        return abonnementService.souscrire(principal.getId(), req);
    }

    @GetMapping
    public List<AbonnementResponse> mesAbonnements(@AuthenticationPrincipal UserPrincipal principal) {
        return abonnementService.mesAbonnements(principal.getId());
    }
}
