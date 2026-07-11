package com.saasclient.service;

import com.saasclient.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    /** Crée une notification pour un utilisateur (n'échoue jamais le traitement appelant). */
    void notifier(Long userId, String titre, String message, String lien);

    List<NotificationResponse> lister(Long userId);

    long compterNonLues(Long userId);

    void marquerLu(Long userId, Long id);

    void marquerToutLu(Long userId);
}
