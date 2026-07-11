package com.saasclient.controller;

import com.saasclient.dto.ClientResponse;
import com.saasclient.dto.ProvisionClientRequest;
import com.saasclient.dto.ProvisionClientResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.ClientService;
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
@RequestMapping("/api/prestataire/clients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PRESTATAIRE')")
public class PrestataireClientController {

    private final ClientService clientService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProvisionClientResponse provisionner(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody ProvisionClientRequest req) {
        return clientService.provisionner(principal.getId(), req);
    }

    @GetMapping
    public Page<ClientResponse> mesClients(@AuthenticationPrincipal UserPrincipal principal,
                                           @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return clientService.listerMesClients(principal.getId(), pageable);
    }

    @PatchMapping("/{id}/desactiver")
    public ClientResponse desactiver(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long id) {
        return clientService.desactiver(principal.getId(), id);
    }

    @PatchMapping("/{id}/reactiver")
    public ClientResponse reactiver(@AuthenticationPrincipal UserPrincipal principal,
                                    @PathVariable Long id) {
        return clientService.reactiver(principal.getId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        clientService.supprimer(principal.getId(), id);
    }
}
