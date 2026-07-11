package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PackResponse {

    private Long id;
    private String nom;
    private String description;
    private Double prix;
    private Integer nbProjetsMax;
    private Integer nbClientsMax;
    private Integer dureeMois;
    private boolean actif;
}
