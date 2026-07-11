package com.saasclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfilResponse {

    private Long userId;
    private String userType;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // ── Spécifique PRESTATAIRE (Expert) ──
    private String specialite;
    private Double tarifHoraire;
    private Boolean disponibilite;
    private List<String> competences;
    private Long packId;
    private String packNom;
    private String statutCompte;

    // ── Spécifique CLIENT (Startup) ──
    private String domaineActivite;
    private String siret;
    private String adresse;
    private Integer nombreEmployes;
    private String identifiantUnique;
}
