package com.saasclient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClientResponse {

    private Long id;          // id de la Startup
    private Long userId;
    private String firstName;
    private String lastName;
    private String identifiantUnique;
    private String domaineActivite;
    private String siret;
    private String adresse;
    private Integer nombreEmployes;
    private boolean actif;
    private LocalDateTime createdAt;
}
