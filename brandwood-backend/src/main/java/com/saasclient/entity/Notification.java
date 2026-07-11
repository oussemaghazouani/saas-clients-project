package com.saasclient.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Notification destinée à un utilisateur (par son id). */
@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 500)
    private String message;

    @Column(length = 200)
    private String lien;

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
