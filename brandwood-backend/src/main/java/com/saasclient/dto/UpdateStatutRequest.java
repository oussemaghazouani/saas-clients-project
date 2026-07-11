package com.saasclient.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateStatutRequest {

    @NotNull(message = "L'action est obligatoire (ACTIVER, DESACTIVER ou SUSPENDRE)")
    private ActionStatut action;
}
