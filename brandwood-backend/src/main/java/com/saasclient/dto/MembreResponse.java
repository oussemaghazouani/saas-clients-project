package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembreResponse {

    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private String roleProjet;
    private Long projetId;
}
