package com.saasclient.mapper;

import com.saasclient.dto.ReunionResponse;
import com.saasclient.entity.Reunion;
import org.springframework.stereotype.Component;

@Component
public class ReunionMapper {

    public ReunionResponse toResponse(Reunion r) {
        return ReunionResponse.builder()
            .id(r.getId())
            .titre(r.getTitre())
            .description(r.getDescription())
            .dateHeure(r.getDateHeure())
            .dureeMinutes(r.getDureeMinutes())
            .lien(r.getLien())
            .statut(r.getStatut().name())
            .projetId(r.getProjet().getId())
            .projetNom(r.getProjet().getNom())
            .build();
    }
}
