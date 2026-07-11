package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Message prive echange entre deux utilisateurs (messagerie directe 1-a-1). */
@Entity
@Table(name = "messages_prives")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessagePrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expediteur_id", nullable = false)
    private User expediteur;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private User destinataire;

    @Column(nullable = false, length = 2000)
    private String contenu;

    @Column(nullable = false)
    @Builder.Default
    private boolean lu = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
