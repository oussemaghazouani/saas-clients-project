package com.saasclient.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProvisionClientResponse {

    private Long startupId;
    private Long userId;
    /** Identifiant unique servant de login au client (à lui communiquer). */
    private String identifiantUnique;
    private String message;
}
