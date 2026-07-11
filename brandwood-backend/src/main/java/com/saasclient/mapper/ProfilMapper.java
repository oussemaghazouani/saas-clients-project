package com.saasclient.mapper;

import com.saasclient.dto.ProfilResponse;
import com.saasclient.entity.Expert;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class ProfilMapper {

    /** Construit la réponse profil ; {@code expert} ou {@code startup} peut être null selon le rôle. */
    public ProfilResponse toResponse(User user, Expert expert, Startup startup) {
        ProfilResponse.ProfilResponseBuilder builder = ProfilResponse.builder()
            .userId(user.getId())
            .userType(user.getUserType().name())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone());

        if (expert != null) {
            builder.specialite(expert.getSpecialite())
                .tarifHoraire(expert.getTarifHoraire())
                .disponibilite(expert.isDisponibilite())
                .competences(new ArrayList<>(expert.getCompetences()))
                .packId(expert.getPack() != null ? expert.getPack().getId() : null)
                .packNom(expert.getPack() != null ? expert.getPack().getNom() : null)
                .statutCompte(expert.getStatutCompte().name());
        }

        if (startup != null) {
            builder.domaineActivite(startup.getDomaineActivite())
                .siret(startup.getSiret())
                .adresse(startup.getAdresse())
                .nombreEmployes(startup.getNombreEmployes())
                .identifiantUnique(startup.getIdentifiantUnique());
        }

        return builder.build();
    }
}
