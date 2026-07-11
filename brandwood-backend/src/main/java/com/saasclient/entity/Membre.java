package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Membre de l'équipe d'un projet (Module 4 — Équipe & Membres). */
@Entity
@Table(name = "membres")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Membre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 180)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_projet", nullable = false, length = 30)
    private RoleProjet roleProjet;

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
