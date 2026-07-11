package com.saasclient.service;

import com.saasclient.dto.RapportProjetResponse;

public interface RapportService {

    /** Génère le rapport agrégé d'un projet (accès : prestataire assigné ou client propriétaire). */
    RapportProjetResponse genererRapportProjet(Long userId, Long projetId);
}
