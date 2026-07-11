package com.saasclient.mapper;

import com.saasclient.dto.PrestataireResponse;
import com.saasclient.entity.Expert;
import com.saasclient.entity.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PrestataireMapper {

    public PrestataireResponse toResponse(Expert expert, long nbClients) {
        User user = expert.getUser();
        return PrestataireResponse.builder()
            .id(expert.getId())
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .specialite(expert.getSpecialite())
            .tarifHoraire(expert.getTarifHoraire())
            .disponibilite(expert.isDisponibilite())
            .competences(new ArrayList<>(expert.getCompetences()))
            .packId(expert.getPack() != null ? expert.getPack().getId() : null)
            .packNom(expert.getPack() != null ? expert.getPack().getNom() : null)
            .statutCompte(expert.getStatutCompte())
            .nbClients(nbClients)
            .createdAt(expert.getCreatedAt())
            .build();
    }
}
