package com.saasclient.mapper;

import com.saasclient.dto.ClientResponse;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public ClientResponse toResponse(Startup startup) {
        User user = startup.getUser();
        return ClientResponse.builder()
            .id(startup.getId())
            .userId(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .identifiantUnique(startup.getIdentifiantUnique())
            .domaineActivite(startup.getDomaineActivite())
            .siret(startup.getSiret())
            .adresse(startup.getAdresse())
            .nombreEmployes(startup.getNombreEmployes())
            .actif(startup.isActif())
            .createdAt(startup.getCreatedAt())
            .build();
    }
}
