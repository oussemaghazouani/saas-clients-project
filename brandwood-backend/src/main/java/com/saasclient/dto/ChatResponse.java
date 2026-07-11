package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatResponse {

    private String reply;
    /** "ia" si généré par le modèle Claude, "assistant" si réponse locale data-aware. */
    private String source;
}
