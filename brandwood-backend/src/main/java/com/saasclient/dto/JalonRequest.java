package com.saasclient.dto;

import com.saasclient.entity.StatutJalon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JalonRequest {

    @NotBlank(message = "Le nom du jalon est obligatoire")
    @Size(max = 150)
    private String nom;

    private LocalDate datePrevue;

    @Size(max = 500)
    private String description;

    /** Optionnel : A_VENIR par défaut. */
    private StatutJalon statut;
}
