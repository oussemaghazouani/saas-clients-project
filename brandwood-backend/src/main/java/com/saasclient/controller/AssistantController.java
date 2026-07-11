package com.saasclient.controller;

import com.saasclient.dto.ChatRequest;
import com.saasclient.dto.ChatResponse;
import com.saasclient.security.UserPrincipal;
import com.saasclient.service.AssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','PRESTATAIRE','SUPER_ADMIN')")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/chat")
    public ChatResponse chat(@AuthenticationPrincipal UserPrincipal principal,
                             @Valid @RequestBody ChatRequest req) {
        return assistantService.repondre(principal.getId(), req.getMessage());
    }
}
