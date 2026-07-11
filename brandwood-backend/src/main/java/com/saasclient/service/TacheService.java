package com.saasclient.service;

import com.saasclient.dto.AnalyseRisqueResponse;
import com.saasclient.dto.GenerationTachesResponse;
import com.saasclient.dto.TacheRequest;
import com.saasclient.dto.TacheResponse;
import com.saasclient.entity.StatutTache;

import java.util.List;

public interface TacheService {

    List<TacheResponse> lister(Long userId, Long projetId);

    TacheResponse creer(Long prestataireUserId, Long projetId, TacheRequest req);

    TacheResponse modifier(Long prestataireUserId, Long tacheId, TacheRequest req);

    TacheResponse changerStatut(Long prestataireUserId, Long tacheId, StatutTache statut);

    void supprimer(Long prestataireUserId, Long tacheId);

    /** IA : génère et crée un découpage de tâches à partir du projet. */
    GenerationTachesResponse genererTaches(Long prestataireUserId, Long projetId);

    /** IA : analyse le risque de retard du projet. */
    AnalyseRisqueResponse analyserRisque(Long userId, Long projetId);
}
