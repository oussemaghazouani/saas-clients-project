package com.saasclient.service;

import com.saasclient.dto.ActionStatut;
import com.saasclient.dto.PrestataireResponse;
import com.saasclient.entity.StatutCompte;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PrestataireAdminService {

    Page<PrestataireResponse> lister(StatutCompte statut, Pageable pageable);

    PrestataireResponse changerStatut(Long expertId, ActionStatut action);

    void supprimer(Long expertId);
}
