package com.saasclient.service;

import com.saasclient.dto.FactureRequest;
import com.saasclient.dto.FactureResponse;
import com.saasclient.entity.StatutFacture;

import java.util.List;

public interface FactureService {

    List<FactureResponse> lister(Long userId);

    FactureResponse obtenir(Long userId, Long id);

    FactureResponse creer(Long prestataireUserId, FactureRequest req);

    FactureResponse changerStatut(Long prestataireUserId, Long id, StatutFacture statut);

    void supprimer(Long prestataireUserId, Long id);
}
