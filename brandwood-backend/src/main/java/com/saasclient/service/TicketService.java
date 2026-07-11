package com.saasclient.service;

import com.saasclient.dto.*;
import com.saasclient.entity.StatutTicket;

import java.util.List;

public interface TicketService {

    List<TicketResponse> lister(Long userId);

    TicketResponse obtenir(Long userId, Long ticketId);

    TicketResponse creer(Long clientUserId, TicketRequest req);

    TicketResponse changerStatut(Long prestataireUserId, Long ticketId, StatutTicket statut);

    MessageResponse ajouterMessage(Long userId, Long ticketId, MessageRequest req);

    /** Analyse IA du ticket (priorité, catégorie, sentiment, réponse suggérée). */
    TicketIaResponse analyser(Long prestataireUserId, Long ticketId);
}
