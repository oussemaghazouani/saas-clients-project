package com.saasclient.service;

import com.saasclient.dto.CampagneIaRequest;
import com.saasclient.dto.CampagneIaResponse;
import com.saasclient.dto.CampagneRequest;
import com.saasclient.dto.CampagneResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CampagneService {

    Page<CampagneResponse> lister(Long userId, Pageable pageable);

    CampagneResponse obtenir(Long userId, Long id);

    CampagneResponse creer(Long prestataireUserId, CampagneRequest req);

    CampagneResponse modifier(Long prestataireUserId, Long id, CampagneRequest req);

    void supprimer(Long prestataireUserId, Long id);

    /** Génère le contenu d'une campagne (accroche, message, audience…) par IA. */
    CampagneIaResponse genererContenu(Long prestataireUserId, CampagneIaRequest req);
}
