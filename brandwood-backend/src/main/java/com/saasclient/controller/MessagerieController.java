package com.saasclient.controller;

import com.saasclient.dto.ContactDto;
import com.saasclient.dto.EnvoiMessageRequest;
import com.saasclient.dto.MessagePriveDto;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.MessagerieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messagerie")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE','SUPER_ADMIN')")
public class MessagerieController {

    private final MessagerieService messagerieService;

    @GetMapping("/contacts")
    public List<ContactDto> contacts(@AuthenticationPrincipal UserPrincipal principal) {
        return messagerieService.contacts(principal.getId());
    }

    @GetMapping("/conversations/{autreUserId}")
    public List<MessagePriveDto> fil(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long autreUserId) {
        return messagerieService.fil(principal.getId(), autreUserId);
    }

    @PostMapping("/messages")
    public MessagePriveDto envoyer(@AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody EnvoiMessageRequest req) {
        return messagerieService.envoyer(principal.getId(), req);
    }

    @GetMapping("/non-lus")
    public Map<String, Long> nonLus(@AuthenticationPrincipal UserPrincipal principal) {
        return Map.of("count", messagerieService.nonLus(principal.getId()));
    }
}
