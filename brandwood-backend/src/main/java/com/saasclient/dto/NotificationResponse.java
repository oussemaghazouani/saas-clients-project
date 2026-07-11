package com.saasclient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {

    private Long id;
    private String titre;
    private String message;
    private String lien;
    private boolean lu;
    private LocalDateTime createdAt;
}
