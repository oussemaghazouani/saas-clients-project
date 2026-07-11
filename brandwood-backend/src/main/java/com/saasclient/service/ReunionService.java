package com.saasclient.service;

import com.saasclient.dto.ReunionRequest;
import com.saasclient.dto.ReunionResponse;

import java.util.List;

public interface ReunionService {

    List<ReunionResponse> mesReunions(Long userId);

    List<ReunionResponse> listerParProjet(Long userId, Long projetId);

    ReunionResponse creer(Long prestataireUserId, Long projetId, ReunionRequest req);

    ReunionResponse modifier(Long prestataireUserId, Long reunionId, ReunionRequest req);

    void supprimer(Long prestataireUserId, Long reunionId);
}
