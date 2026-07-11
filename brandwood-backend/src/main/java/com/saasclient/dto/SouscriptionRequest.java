package com.saasclient.dto;

import com.saasclient.entity.MethodePaiement;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SouscriptionRequest {

    @NotNull(message = "Le pack est obligatoire")
    private Long packId;

    @NotNull(message = "La méthode de paiement est obligatoire")
    private MethodePaiement methodePaiement;
}
