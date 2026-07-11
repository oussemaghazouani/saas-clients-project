package com.saasclient.mapper;

import com.saasclient.dto.JalonResponse;
import com.saasclient.entity.Jalon;
import com.saasclient.entity.StatutJalon;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class JalonMapper {

    public JalonResponse toResponse(Jalon j) {
        return JalonResponse.builder()
            .id(j.getId())
            .nom(j.getNom())
            .datePrevue(j.getDatePrevue())
            .statut(statutAffiche(j))
            .description(j.getDescription())
            .projetId(j.getProjet().getId())
            .build();
    }

    /** EN_RETARD dérivé : date prévue passée et jalon non ATTEINT. */
    private String statutAffiche(Jalon j) {
        if (j.getStatut() != StatutJalon.ATTEINT
                && j.getDatePrevue() != null
                && j.getDatePrevue().isBefore(LocalDate.now())) {
            return StatutJalon.EN_RETARD.name();
        }
        return j.getStatut().name();
    }
}
