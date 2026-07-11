package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Message dans le fil de discussion d'un ticket. */
@Entity
@Table(name = "messages_ticket")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MessageTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(name = "auteur_nom", length = 150)
    private String auteurNom;

    @Column(name = "auteur_role", length = 20)
    private String auteurRole;

    @Column(nullable = false, length = 1000)
    private String contenu;

    @Column(name = "date_envoi", nullable = false, updatable = false)
    private LocalDateTime dateEnvoi;

    @PrePersist
    protected void onCreate() {
        dateEnvoi = LocalDateTime.now();
    }
}
