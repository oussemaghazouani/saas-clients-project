package com.saasclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Requete d'envoi d'un message prive. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnvoiMessageRequest {

    @NotNull(message = "Le destinataire est obligatoire")
    private Long destinataireId;

    @NotBlank(message = "Le message ne peut pas etre vide")
    @Size(max = 2000)
    private String contenu;
}
