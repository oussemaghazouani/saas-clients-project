package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

/** Ligne d'une facture (prestation facturée). */
@Entity
@Table(name = "lignes_facture")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Double quantite = 1.0;

    @Column(name = "prix_unitaire", nullable = false)
    @Builder.Default
    private Double prixUnitaire = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double montant = 0.0;
}
