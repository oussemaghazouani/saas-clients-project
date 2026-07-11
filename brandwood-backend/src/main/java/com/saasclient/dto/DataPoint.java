package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DataPoint {
    private String label;
    private double valeur;
    private String couleur;
}
