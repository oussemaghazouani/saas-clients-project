package com.saasclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {

    private Long id;
    private String sujet;
    private String description;
    private String statut;
    private String priorite;
    private Long startupId;
    private String clientNom;
    private Long expertId;
    private String prestataireNom;
    private long nbMessages;
    private LocalDateTime createdAt;

    /** Rempli uniquement sur le détail d'un ticket. */
    private List<MessageResponse> messages;
}
