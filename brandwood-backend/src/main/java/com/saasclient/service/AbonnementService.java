package com.saasclient.service;

import com.saasclient.dto.AbonnementResponse;
import com.saasclient.dto.SouscriptionRequest;
import com.saasclient.entity.StatutAbonnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AbonnementService {

    AbonnementResponse souscrire(Long prestataireUserId, SouscriptionRequest req);

    List<AbonnementResponse> mesAbonnements(Long prestataireUserId);

    Page<AbonnementResponse> listerParStatut(StatutAbonnement statut, Pageable pageable);

    AbonnementResponse validerVirement(Long abonnementId);
}
