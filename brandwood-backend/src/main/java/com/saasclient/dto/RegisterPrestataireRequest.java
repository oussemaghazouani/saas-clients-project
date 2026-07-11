package com.saasclient.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterPrestataireRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 180)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre"
    )
    private String password;

    @Size(max = 30)
    private String phone;

    @NotBlank(message = "La spécialité / poste est obligatoire")
    @Size(max = 150)
    private String jobTitle;

    @Size(max = 150)
    private String companyName;
}
