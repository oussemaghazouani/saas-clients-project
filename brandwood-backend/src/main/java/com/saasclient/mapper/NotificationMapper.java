package com.saasclient.mapper;

import com.saasclient.dto.NotificationResponse;
import com.saasclient.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .titre(n.getTitre())
            .message(n.getMessage())
            .lien(n.getLien())
            .lu(n.isLu())
            .createdAt(n.getCreatedAt())
            .build();
    }
}
