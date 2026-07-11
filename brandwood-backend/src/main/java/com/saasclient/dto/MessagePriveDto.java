package com.saasclient.dto;

import lombok.*;

/** Un message prive tel que renvoye au frontend (REST ou WebSocket). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessagePriveDto {
    private Long id;
    private Long expediteurId;
    private String expediteurNom;
    private Long destinataireId;
    private String contenu;
    private boolean lu;
    private String dateEnvoi;
}
