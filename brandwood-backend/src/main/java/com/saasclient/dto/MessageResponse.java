package com.saasclient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponse {

    private Long id;
    private String auteurNom;
    private String auteurRole;
    private String contenu;
    private LocalDateTime dateEnvoi;
}
