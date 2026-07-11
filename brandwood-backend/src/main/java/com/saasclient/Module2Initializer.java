package com.saasclient;

import com.saasclient.entity.Expert;
import com.saasclient.entity.StatutCompte;
import com.saasclient.entity.User;
import com.saasclient.entity.UserType;
import com.saasclient.repository.ExpertRepository;
import com.saasclient.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Provisionne un profil {@link Expert} (statut ACTIF) pour chaque PRESTATAIRE existant
 * issu du module Auth qui n'en aurait pas encore — afin qu'ils apparaissent côté SUPER_ADMIN.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(100)
public class Module2Initializer {

    private final UserRepository userRepository;
    private final ExpertRepository expertRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {
        List<User> prestataires = userRepository.findByUserType(UserType.PRESTATAIRE);
        int crees = 0;
        for (User user : prestataires) {
            if (!expertRepository.existsByUserId(user.getId())) {
                expertRepository.save(Expert.builder()
                    .user(user)
                    .disponibilite(true)
                    .competences(new ArrayList<>())
                    .statutCompte(StatutCompte.ACTIF)
                    .build());
                crees++;
            }
        }
        if (crees > 0) {
            log.info("Module 2 : {} profil(s) Expert créé(s) pour des prestataires existants.", crees);
        }
    }
}
