package com.saasclient.dto;

import com.saasclient.entity.CanalCampagne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/** Brief fourni à l'IA pour générer le contenu d'une campagne marketing. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CampagneIaRequest {

    @NotBlank(message = "Le thème / produit est obligatoire")
    @Size(max = 300)
    private String theme;

    /** Canal visé (optionnel : l'IA en recommande un si absent). */
    private CanalCampagne canal;

    /** Cible / audience visée (optionnel). */
    @Size(max = 200)
    private String cible;
}
