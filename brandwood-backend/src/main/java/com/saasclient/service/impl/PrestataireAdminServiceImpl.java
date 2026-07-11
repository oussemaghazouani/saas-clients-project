package com.saasclient.service.impl;

import com.saasclient.dto.ActionStatut;
import com.saasclient.dto.PrestataireResponse;
import com.saasclient.entity.Expert;
import com.saasclient.entity.StatutCompte;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.PrestataireMapper;
import com.saasclient.repository.AbonnementRepository;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.PrestataireAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestataireAdminServiceImpl implements PrestataireAdminService {

    private final ExpertRepository expertRepository;
    private final StartupRepository startupRepository;
    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final PrestataireMapper prestataireMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<PrestataireResponse> lister(StatutCompte statut, Pageable pageable) {
        Page<Expert> page = (statut == null)
            ? expertRepository.findAll(pageable)
            : expertRepository.findByStatutCompte(statut, pageable);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public PrestataireResponse changerStatut(Long expertId, ActionStatut action) {
        Expert expert = getExpert(expertId);
        StatutCompte nouveau = switch (action) {
            case ACTIVER    -> StatutCompte.ACTIF;
            case DESACTIVER -> StatutCompte.DESACTIVE;
            case SUSPENDRE  -> StatutCompte.SUSPENDU;
        };
        expert.setStatutCompte(nouveau);
        User user = expert.getUser();
        user.setEnabled(nouveau == StatutCompte.ACTIF);
        userRepository.save(user);
        return toResponse(expertRepository.save(expert));
    }

    @Override
    @Transactional
    public void supprimer(Long expertId) {
        Expert expert = getExpert(expertId);
        Long userId = expert.getUser().getId();

        // Les clients sont conservés (historique) mais détachés du prestataire supprimé.
        List<Startup> clients = startupRepository.findByPrestataireId(expertId);
        clients.forEach(c -> c.setPrestataire(null));
        startupRepository.saveAll(clients);

        abonnementRepository.deleteByExpertId(expertId);
        expertRepository.delete(expert);
        userRepository.deleteById(userId);
    }

    private PrestataireResponse toResponse(Expert expert) {
        long nbClients = startupRepository.countByPrestataireIdAndActifTrue(expert.getId());
        return prestataireMapper.toResponse(expert, nbClients);
    }

    private Expert getExpert(Long id) {
        return expertRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Prestataire introuvable."));
    }
}
