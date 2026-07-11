package com.saasclient.mapper;

import com.saasclient.dto.PackRequest;
import com.saasclient.dto.PackResponse;
import com.saasclient.entity.Pack;
import org.springframework.stereotype.Component;

@Component
public class PackMapper {

    public PackResponse toResponse(Pack pack) {
        return PackResponse.builder()
            .id(pack.getId())
            .nom(pack.getNom())
            .description(pack.getDescription())
            .prix(pack.getPrix())
            .nbProjetsMax(pack.getNbProjetsMax())
            .nbClientsMax(pack.getNbClientsMax())
            .dureeMois(pack.getDureeMois())
            .actif(pack.isActif())
            .build();
    }

    public Pack toEntity(PackRequest req) {
        return Pack.builder()
            .nom(req.getNom())
            .description(req.getDescription())
            .prix(req.getPrix())
            .nbProjetsMax(req.getNbProjetsMax())
            .nbClientsMax(req.getNbClientsMax())
            .dureeMois(req.getDureeMois())
            .actif(req.getActif() == null || req.getActif())
            .build();
    }

    public void updateEntity(Pack pack, PackRequest req) {
        pack.setNom(req.getNom());
        pack.setDescription(req.getDescription());
        pack.setPrix(req.getPrix());
        pack.setNbProjetsMax(req.getNbProjetsMax());
        pack.setNbClientsMax(req.getNbClientsMax());
        pack.setDureeMois(req.getDureeMois());
        pack.setActif(req.getActif() == null || req.getActif());
    }
}
