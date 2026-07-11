package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Abonnement d'un prestataire (Expert) à un Pack. */
@Entity
@Table(name = "abonnements")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pack_id", nullable = false)
    private Pack pack;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAbonnement statut;

    @Enumerated(EnumType.STRING)
    @Column(name = "methode_paiement", nullable = false, length = 20)
    private MethodePaiement methodePaiement;

    @Column(name = "essai_gratuit", nullable = false)
    @Builder.Default
    private boolean essaiGratuit = false;

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
