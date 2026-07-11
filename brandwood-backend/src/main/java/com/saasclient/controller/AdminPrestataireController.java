package com.saasclient.controller;

import com.saasclient.dto.PrestataireResponse;
import com.saasclient.dto.UpdateStatutRequest;
import com.saasclient.entity.StatutCompte;
import com.saasclient.service.PrestataireAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/prestataires")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminPrestataireController {

    private final PrestataireAdminService prestataireAdminService;

    @GetMapping
    public Page<PrestataireResponse> lister(@RequestParam(required = false) StatutCompte statut,
                                            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return prestataireAdminService.lister(statut, pageable);
    }

    @PatchMapping("/{id}/statut")
    public PrestataireResponse changerStatut(@PathVariable Long id,
                                             @Valid @RequestBody UpdateStatutRequest req) {
        return prestataireAdminService.changerStatut(id, req.getAction());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        prestataireAdminService.supprimer(id);
    }
}
