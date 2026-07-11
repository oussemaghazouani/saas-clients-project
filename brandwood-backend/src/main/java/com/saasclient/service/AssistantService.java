package com.saasclient.service;

import com.saasclient.dto.ChatResponse;

public interface AssistantService {

    /** Répond à un message de l'utilisateur authentifié (assistant data-aware, Claude optionnel). */
    ChatResponse repondre(Long userId, String message);
}
