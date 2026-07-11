package com.saasclient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRequest {

    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 1000)
    private String message;
}
