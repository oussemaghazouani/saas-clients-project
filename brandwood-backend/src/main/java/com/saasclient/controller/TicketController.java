package com.saasclient.controller;

import com.saasclient.dto.*;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public List<TicketResponse> lister(@AuthenticationPrincipal UserPrincipal principal) {
        return ticketService.lister(principal.getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    public TicketResponse obtenir(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ticketService.obtenir(principal.getId(), id);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse creer(@AuthenticationPrincipal UserPrincipal principal,
                                @Valid @RequestBody TicketRequest req) {
        return ticketService.creer(principal.getId(), req);
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public TicketResponse changerStatut(@AuthenticationPrincipal UserPrincipal principal,
                                        @PathVariable Long id, @Valid @RequestBody TicketStatutRequest req) {
        return ticketService.changerStatut(principal.getId(), id, req.getStatut());
    }

    @PostMapping("/{id}/messages")
    @PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE')")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse ajouterMessage(@AuthenticationPrincipal UserPrincipal principal,
                                          @PathVariable Long id, @Valid @RequestBody MessageRequest req) {
        return ticketService.ajouterMessage(principal.getId(), id, req);
    }

    @PostMapping("/{id}/ia/analyser")
    @PreAuthorize("hasRole('PRESTATAIRE')")
    public TicketIaResponse analyser(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return ticketService.analyser(principal.getId(), id);
    }
}
