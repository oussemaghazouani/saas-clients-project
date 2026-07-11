package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Profil métier d'un PRESTATAIRE. Composition (non héritage) avec le compte {@link User}
 * réutilisé du module Auth : un Expert référence un User via @OneToOne.
 */
@Entity
@Table(name = "experts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Expert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 150)
    private String specialite;

    @Column(name = "tarif_horaire")
    private Double tarifHoraire;

    @Column(nullable = false)
    @Builder.Default
    private boolean disponibilite = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expert_competences", joinColumns = @JoinColumn(name = "expert_id"))
    @Column(name = "competence", length = 100)
    @Builder.Default
    private List<String> competences = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pack_id")
    private Pack pack;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte", nullable = false, length = 20)
    @Builder.Default
    private StatutCompte statutCompte = StatutCompte.ACTIF;

    /** Date de début de l'essai gratuit (fixée à la 1ère provision sans pack actif). */
    @Column(name = "date_debut_essai")
    private LocalDate dateDebutEssai;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
