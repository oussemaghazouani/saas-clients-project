package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Offre commerciale souscrite par les prestataires (Experts). */
@Entity
@Table(name = "packs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Pack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Double prix;

    @Column(name = "nb_projets_max", nullable = false)
    private Integer nbProjetsMax;

    @Column(name = "nb_clients_max", nullable = false)
    private Integer nbClientsMax;

    @Column(name = "duree_mois", nullable = false)
    private Integer dureeMois;

    @Column(nullable = false)
    @Builder.Default
    private boolean actif = true;

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
