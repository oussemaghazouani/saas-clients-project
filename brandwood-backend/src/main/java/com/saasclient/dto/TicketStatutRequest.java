package com.saasclient.dto;

import com.saasclient.entity.StatutTicket;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutTicket statut;
}
