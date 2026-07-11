package com.saasclient.mapper;

import com.saasclient.dto.TacheResponse;
import com.saasclient.entity.StatutTache;
import com.saasclient.entity.Tache;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TacheMapper {

    public TacheResponse toResponse(Tache t) {
        return TacheResponse.builder()
            .id(t.getId())
            .titre(t.getTitre())
            .description(t.getDescription())
            .statut(t.getStatut().name())
            .priorite(t.getPriorite().name())
            .dateEcheance(t.getDateEcheance())
            .enRetard(estEnRetard(t))
            .position(t.getPosition())
            .projetId(t.getProjet().getId())
            .membreId(t.getMembre() != null ? t.getMembre().getId() : null)
            .membreNom(t.getMembre() != null
                ? t.getMembre().getPrenom() + " " + t.getMembre().getNom() : null)
            .build();
    }

    private boolean estEnRetard(Tache t) {
        return t.getStatut() != StatutTache.TERMINE
            && t.getDateEcheance() != null
            && t.getDateEcheance().isBefore(LocalDate.now());
    }
}
