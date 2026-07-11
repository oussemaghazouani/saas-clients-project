package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Réunion planifiée dans le cadre d'un projet (Module 6 — Calendrier & Réunions). */
@Entity
@Table(name = "reunions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Column(name = "date_heure", nullable = false)
    private LocalDateTime dateHeure;

    @Column(name = "duree_minutes")
    @Builder.Default
    private Integer dureeMinutes = 60;

    /** Lien de visioconférence (optionnel). */
    @Column(length = 500)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutReunion statut = StatutReunion.PLANIFIEE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

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
