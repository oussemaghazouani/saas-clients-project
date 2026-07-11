package com.saasclient.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Mise à jour du profil propre. Les champs Expert ne sont appliqués que pour un PRESTATAIRE,
 * les champs Startup que pour un CLIENT (filtrage côté service selon le rôle authentifié).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateProfilRequest {

    // ── Champs communs (User) ──
    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 30)
    private String phone;

    // ── Champs PRESTATAIRE (Expert) ──
    @Size(max = 150)
    private String specialite;

    @PositiveOrZero(message = "Le tarif horaire doit être positif ou nul")
    private Double tarifHoraire;

    private Boolean disponibilite;

    private List<String> competences;

    // ── Champs CLIENT (Startup) ──
    @Size(max = 150)
    private String domaineActivite;

    @Size(max = 20)
    private String siret;

    @Size(max = 255)
    private String adresse;

    @PositiveOrZero(message = "Le nombre d'employés doit être positif ou nul")
    private Integer nombreEmployes;
}
