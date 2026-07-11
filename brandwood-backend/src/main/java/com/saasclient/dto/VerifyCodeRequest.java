package com.saasclient.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerifyCodeRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le code est obligatoire")
    @Pattern(regexp = "^\\d{6}$", message = "Le code doit contenir exactement 6 chiffres")
    private String code;
}
