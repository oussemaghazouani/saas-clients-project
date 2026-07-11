package com.saasclient.service;

import com.saasclient.dto.ContactDto;
import com.saasclient.dto.EnvoiMessageRequest;
import com.saasclient.dto.MessagePriveDto;

import java.util.List;

public interface MessagerieService {

    /** Correspondants autorises + apercu du dernier echange et compteur de non-lus. */
    List<ContactDto> contacts(Long userId);

    /** Fil complet avec un correspondant (marque les messages recus comme lus). */
    List<MessagePriveDto> fil(Long userId, Long autreUserId);

    /** Envoie un message (persistance + push WebSocket au destinataire). */
    MessagePriveDto envoyer(Long expediteurUserId, EnvoiMessageRequest req);

    /** Nombre total de messages non lus (pour le badge). */
    long nonLus(Long userId);
}
