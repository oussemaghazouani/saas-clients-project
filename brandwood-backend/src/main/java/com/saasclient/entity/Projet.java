package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Projet IT : appartient à un Client ({@link Startup}), géré par un Prestataire ({@link Expert}),
 * porte des {@link Jalon}s et une progression calculée.
 */
@Entity
@Table(name = "projets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 1000)
    private String description;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    private Double budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutProjet statut = StatutProjet.PLANIFIE;

    /** Progression 0–100, recalculée à partir des jalons ATTEINT / total. */
    @Column(nullable = false)
    @Builder.Default
    private Double progression = 0.0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "projet_technologies", joinColumns = @JoinColumn(name = "projet_id"))
    @Column(name = "technologie", length = 100)
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "startup_id", nullable = false)
    private Startup startup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expert_id", nullable = false)
    private Expert expert;

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
