package com.saasclient.mapper;

import com.saasclient.dto.CampagneResponse;
import com.saasclient.entity.Campagne;
import org.springframework.stereotype.Component;

@Component
public class CampagneMapper {

    public CampagneResponse toResponse(Campagne c) {
        int impressions = c.getImpressions() != null ? c.getImpressions() : 0;
        int clics = c.getClics() != null ? c.getClics() : 0;
        int conversions = c.getConversions() != null ? c.getConversions() : 0;
        double taux = clics > 0 ? Math.round((1000.0 * conversions) / clics) / 10.0 : 0.0;
        double ctr = impressions > 0 ? Math.round((1000.0 * clics) / impressions) / 10.0 : 0.0;

        return CampagneResponse.builder()
            .id(c.getId())
            .nom(c.getNom())
            .description(c.getDescription())
            .canal(c.getCanal().name())
            .statut(c.getStatut().name())
            .budget(c.getBudget())
            .dateDebut(c.getDateDebut())
            .dateFin(c.getDateFin())
            .impressions(impressions)
            .clics(clics)
            .conversions(conversions)
            .tauxConversion(taux)
            .ctr(ctr)
            .startupId(c.getStartup().getId())
            .clientNom(c.getStartup().getUser().getFirstName() + " " + c.getStartup().getUser().getLastName())
            .expertId(c.getExpert().getId())
            .prestataireNom(c.getExpert().getUser().getFirstName() + " " + c.getExpert().getUser().getLastName())
            .createdAt(c.getCreatedAt())
            .build();
    }
}
