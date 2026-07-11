package com.saasclient.controller;

import com.saasclient.dto.AbonnementResponse;
import com.saasclient.entity.StatutAbonnement;
import com.saasclient.service.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/abonnements")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminAbonnementController {

    private final AbonnementService abonnementService;

    @GetMapping
    public Page<AbonnementResponse> lister(@RequestParam(required = false) StatutAbonnement statut,
                                           @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return abonnementService.listerParStatut(statut, pageable);
    }

    @PatchMapping("/{id}/valider")
    public AbonnementResponse valider(@PathVariable Long id) {
        return abonnementService.validerVirement(id);
    }
}
