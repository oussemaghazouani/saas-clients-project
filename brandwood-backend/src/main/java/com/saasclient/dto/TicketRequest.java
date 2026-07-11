package com.saasclient.dto;

import com.saasclient.entity.PrioriteTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketRequest {

    @NotBlank(message = "Le sujet est obligatoire")
    @Size(max = 200)
    private String sujet;

    @Size(max = 1000)
    private String description;

    /** Optionnel : MOYENNE par défaut. */
    private PrioriteTicket priorite;
}
