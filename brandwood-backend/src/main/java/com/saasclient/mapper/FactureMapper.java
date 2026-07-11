package com.saasclient.mapper;

import com.saasclient.dto.FactureResponse;
import com.saasclient.dto.LigneResponse;
import com.saasclient.entity.Facture;
import com.saasclient.entity.LigneFacture;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FactureMapper {

    public FactureResponse toResponse(Facture f) {
        return FactureResponse.builder()
            .id(f.getId())
            .numero(f.getNumero())
            .startupId(f.getStartup().getId())
            .clientNom(f.getStartup().getUser().getFirstName() + " " + f.getStartup().getUser().getLastName())
            .expertId(f.getExpert().getId())
            .prestataireNom(f.getExpert().getUser().getFirstName() + " " + f.getExpert().getUser().getLastName())
            .projetId(f.getProjet() != null ? f.getProjet().getId() : null)
            .projetNom(f.getProjet() != null ? f.getProjet().getNom() : null)
            .montantHt(f.getMontantHt())
            .tauxTva(f.getTauxTva())
            .montantTtc(f.getMontantTtc())
            .statut(f.getStatut().name())
            .dateEmission(f.getDateEmission())
            .dateEcheance(f.getDateEcheance())
            .createdAt(f.getCreatedAt())
            .lignes(f.getLignes().stream().map(this::ligne).collect(Collectors.toList()))
            .build();
    }

    private LigneResponse ligne(LigneFacture l) {
        return LigneResponse.builder()
            .id(l.getId())
            .description(l.getDescription())
            .quantite(l.getQuantite())
            .prixUnitaire(l.getPrixUnitaire())
            .montant(l.getMontant())
            .build();
    }
}
