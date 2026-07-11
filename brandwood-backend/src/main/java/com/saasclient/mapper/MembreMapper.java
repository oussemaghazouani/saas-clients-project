package com.saasclient.mapper;

import com.saasclient.dto.MembreResponse;
import com.saasclient.entity.Membre;
import org.springframework.stereotype.Component;

@Component
public class MembreMapper {

    public MembreResponse toResponse(Membre m) {
        return MembreResponse.builder()
            .id(m.getId())
            .prenom(m.getPrenom())
            .nom(m.getNom())
            .email(m.getEmail())
            .roleProjet(m.getRoleProjet().name())
            .projetId(m.getProjet().getId())
            .build();
    }
}
