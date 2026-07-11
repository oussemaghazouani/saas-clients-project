package com.saasclient.mapper;

import com.saasclient.dto.ProjetResponse;
import com.saasclient.entity.Projet;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProjetMapper {

    public ProjetResponse toResponse(Projet p, long nbJalons, long jalonsAtteints) {
        User prestataire = p.getExpert().getUser();
        return ProjetResponse.builder()
            .id(p.getId())
            .nom(p.getNom())
            .description(p.getDescription())
            .dateDebut(p.getDateDebut())
            .dateFin(p.getDateFin())
            .budget(p.getBudget())
            .statut(p.getStatut())
            .progression(p.getProgression())
            .technologies(new ArrayList<>(p.getTechnologies()))
            .startupId(p.getStartup().getId())
            .clientNom(clientNom(p.getStartup()))
            .expertId(p.getExpert().getId())
            .prestataireNom(prestataire.getFirstName() + " " + prestataire.getLastName())
            .nbJalons(nbJalons)
            .jalonsAtteints(jalonsAtteints)
            .createdAt(p.getCreatedAt())
            .build();
    }

    private String clientNom(Startup startup) {
        User u = startup.getUser();
        return u.getFirstName() + " " + u.getLastName();
    }
}
