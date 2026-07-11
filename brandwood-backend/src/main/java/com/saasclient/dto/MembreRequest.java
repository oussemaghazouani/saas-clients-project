package com.saasclient.dto;

import com.saasclient.entity.RoleProjet;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MembreRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @Email(message = "Format d'email invalide")
    @Size(max = 180)
    private String email;

    @NotNull(message = "Le rôle du membre est obligatoire")
    private RoleProjet roleProjet;
}
