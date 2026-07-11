package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Tâche d'un projet, positionnée dans une colonne du tableau Kanban. */
@Entity
@Table(name = "taches")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutTache statut = StatutTache.A_FAIRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PrioriteTache priorite = PrioriteTache.MOYENNE;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(nullable = false)
    @Builder.Default
    private int position = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "projet_id", nullable = false)
    private Projet projet;

    /** Membre de l'équipe assigné (optionnel). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "membre_id")
    private Membre membre;

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
