package com.saasclient.mapper;

import com.saasclient.dto.AbonnementResponse;
import com.saasclient.entity.Abonnement;
import com.saasclient.entity.Expert;
import com.saasclient.entity.User;
import org.springframework.stereotype.Component;

@Component
public class AbonnementMapper {

    public AbonnementResponse toResponse(Abonnement abonnement) {
        Expert expert = abonnement.getExpert();
        User user = expert.getUser();
        return AbonnementResponse.builder()
            .id(abonnement.getId())
            .expertId(expert.getId())
            .prestataireNom(user.getFirstName() + " " + user.getLastName())
            .packId(abonnement.getPack().getId())
            .packNom(abonnement.getPack().getNom())
            .dateDebut(abonnement.getDateDebut())
            .dateFin(abonnement.getDateFin())
            .statut(abonnement.getStatut().name())
            .methodePaiement(abonnement.getMethodePaiement().name())
            .essaiGratuit(abonnement.isEssaiGratuit())
            .build();
    }
}
