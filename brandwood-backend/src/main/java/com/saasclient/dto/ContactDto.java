package com.saasclient.dto;

import lombok.*;

/** Un correspondant possible dans la messagerie + son dernier echange. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ContactDto {
    private Long userId;
    private String nom;
    private String role;            // CLIENT | PRESTATAIRE | SUPER_ADMIN
    private String dernierMessage;  // apercu (nullable)
    private String dernierMessageDate;
    private long nonLus;            // messages non lus recus de ce contact
}
