package com.saasclient.dto;

import com.saasclient.entity.StatutCompte;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PrestataireResponse {

    private Long id;          // id de l'Expert
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String specialite;
    private Double tarifHoraire;
    private boolean disponibilite;
    private List<String> competences;
    private Long packId;
    private String packNom;
    private StatutCompte statutCompte;
    private long nbClients;
    private LocalDateTime createdAt;
}
