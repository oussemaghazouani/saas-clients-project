package com.saasclient.service;

import com.saasclient.dto.JalonRequest;
import com.saasclient.dto.JalonResponse;
import com.saasclient.dto.MembreRequest;
import com.saasclient.dto.MembreResponse;
import com.saasclient.dto.ProjetRequest;
import com.saasclient.dto.ProjetResponse;
import com.saasclient.entity.StatutJalon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjetService {

    // ── Projets ──
    Page<ProjetResponse> lister(Long userId, Pageable pageable);

    ProjetResponse obtenir(Long userId, Long projetId);

    ProjetResponse creer(Long prestataireUserId, ProjetRequest req);

    ProjetResponse modifier(Long prestataireUserId, Long projetId, ProjetRequest req);

    void supprimer(Long prestataireUserId, Long projetId);

    // ── Jalons ──
    List<JalonResponse> listerJalons(Long userId, Long projetId);

    JalonResponse ajouterJalon(Long prestataireUserId, Long projetId, JalonRequest req);

    JalonResponse modifierJalon(Long prestataireUserId, Long jalonId, JalonRequest req);

    JalonResponse changerStatutJalon(Long prestataireUserId, Long jalonId, StatutJalon statut);

    void supprimerJalon(Long prestataireUserId, Long jalonId);

    // ── Membres (équipe) ──
    List<MembreResponse> listerMembres(Long userId, Long projetId);

    MembreResponse ajouterMembre(Long prestataireUserId, Long projetId, MembreRequest req);

    MembreResponse modifierMembre(Long prestataireUserId, Long membreId, MembreRequest req);

    void supprimerMembre(Long prestataireUserId, Long membreId);
}
