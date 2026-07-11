package com.saasclient.mapper;

import com.saasclient.dto.MessageResponse;
import com.saasclient.entity.MessageTicket;
import org.springframework.stereotype.Component;

@Component
public class MessageTicketMapper {

    public MessageResponse toResponse(MessageTicket m) {
        return MessageResponse.builder()
            .id(m.getId())
            .auteurNom(m.getAuteurNom())
            .auteurRole(m.getAuteurRole())
            .contenu(m.getContenu())
            .dateEnvoi(m.getDateEnvoi())
            .build();
    }
}
