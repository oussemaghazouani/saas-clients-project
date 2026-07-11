package com.saasclient.dto;

import com.saasclient.entity.PrioriteTache;
import com.saasclient.entity.StatutTache;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TacheRequest {

    @NotBlank(message = "Le titre de la tâche est obligatoire")
    @Size(max = 200)
    private String titre;

    @Size(max = 1000)
    private String description;

    /** Optionnel : MOYENNE par défaut. */
    private PrioriteTache priorite;

    private LocalDate dateEcheance;

    /** Optionnel : A_FAIRE par défaut. */
    private StatutTache statut;

    /** Membre de l'équipe assigné (optionnel). */
    private Long membreId;
}
