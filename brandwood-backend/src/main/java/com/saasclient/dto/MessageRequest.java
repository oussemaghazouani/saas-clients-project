package com.saasclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 1000)
    private String contenu;
}
