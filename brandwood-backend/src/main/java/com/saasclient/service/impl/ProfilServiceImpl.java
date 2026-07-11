package com.saasclient.service.impl;

import com.saasclient.dto.ProfilResponse;
import com.saasclient.dto.UpdateProfilRequest;
import com.saasclient.entity.Expert;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import com.saasclient.entity.UserType;
import com.saasclient.exception.ResourceNotFoundException;
import com.saasclient.mapper.ProfilMapper;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import com.saasclient.service.ProfilService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfilServiceImpl implements ProfilService {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final ExpertProfileService expertProfileService;
    private final ProfilMapper profilMapper;

    @Override
    @Transactional
    public ProfilResponse monProfil(Long userId) {
        User user = getUser(userId);
        Expert expert = null;
        Startup startup = null;
        if (user.getUserType() == UserType.PRESTATAIRE) {
            expert = expertProfileService.getOrCreate(userId);
        } else if (user.getUserType() == UserType.CLIENT) {
            startup = startupRepository.findByUserId(userId).orElse(null);
        }
        return profilMapper.toResponse(user, expert, startup);
    }

    @Override
    @Transactional
    public ProfilResponse mettreAJour(Long userId, UpdateProfilRequest req) {
        User user = getUser(userId);
        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null)  user.setLastName(req.getLastName());
        if (req.getPhone() != null)     user.setPhone(req.getPhone());

        Expert expert = null;
        Startup startup = null;

        if (user.getUserType() == UserType.PRESTATAIRE) {
            expert = expertProfileService.getOrCreate(userId);
            if (req.getSpecialite() != null)    expert.setSpecialite(req.getSpecialite());
            if (req.getTarifHoraire() != null)  expert.setTarifHoraire(req.getTarifHoraire());
            if (req.getDisponibilite() != null) expert.setDisponibilite(req.getDisponibilite());
            if (req.getCompetences() != null) {
                expert.getCompetences().clear();
                expert.getCompetences().addAll(req.getCompetences());
            }
        } else if (user.getUserType() == UserType.CLIENT) {
            startup = startupRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil client introuvable."));
            if (req.getDomaineActivite() != null) startup.setDomaineActivite(req.getDomaineActivite());
            if (req.getSiret() != null)           startup.setSiret(req.getSiret());
            if (req.getAdresse() != null)          startup.setAdresse(req.getAdresse());
            if (req.getNombreEmployes() != null)   startup.setNombreEmployes(req.getNombreEmployes());
        }

        userRepository.save(user);
        return profilMapper.toResponse(user, expert, startup);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
    }
}
