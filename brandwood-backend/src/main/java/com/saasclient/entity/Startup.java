package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Profil métier d'un CLIENT. Composition avec le compte {@link User} réutilisé du module Auth.
 * Provisionné par un {@link Expert} ; l'{@code identifiantUnique} sert d'accès (login) au client.
 */
@Entity
@Table(name = "startups")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "domaine_activite", length = 150)
    private String domaineActivite;

    @Column(length = 20)
    private String siret;

    @Column(length = 255)
    private String adresse;

    @Column(name = "nombre_employes")
    private Integer nombreEmployes;

    @Column(name = "identifiant_unique", nullable = false, unique = true, length = 100)
    private String identifiantUnique;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prestataire_id")
    private Expert prestataire;

    /** Flag de soft-delete : false = client désactivé (conservé dans l'historique). */
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
