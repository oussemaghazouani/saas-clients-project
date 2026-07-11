package com.saasclient.mapper;

import com.saasclient.dto.TicketResponse;
import com.saasclient.entity.MessageTicket;
import com.saasclient.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TicketMapper {

    private final MessageTicketMapper messageMapper;

    /** {@code messages} null => vue liste ; non null => vue détail. */
    public TicketResponse toResponse(Ticket t, long nbMessages, List<MessageTicket> messages) {
        return TicketResponse.builder()
            .id(t.getId())
            .sujet(t.getSujet())
            .description(t.getDescription())
            .statut(t.getStatut().name())
            .priorite(t.getPriorite().name())
            .startupId(t.getStartup().getId())
            .clientNom(t.getStartup().getUser().getFirstName() + " " + t.getStartup().getUser().getLastName())
            .expertId(t.getExpert().getId())
            .prestataireNom(t.getExpert().getUser().getFirstName() + " " + t.getExpert().getUser().getLastName())
            .nbMessages(nbMessages)
            .createdAt(t.getCreatedAt())
            .messages(messages == null ? null
                : messages.stream().map(messageMapper::toResponse).collect(Collectors.toList()))
            .build();
    }
}
