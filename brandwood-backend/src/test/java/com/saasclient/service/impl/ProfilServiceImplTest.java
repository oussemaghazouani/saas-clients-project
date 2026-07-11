package com.saasclient.service.impl;

import com.saasclient.dto.UpdateProfilRequest;
import com.saasclient.entity.Startup;
import com.saasclient.entity.User;
import com.saasclient.entity.UserType;
import com.saasclient.mapper.ProfilMapper;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private StartupRepository startupRepository;
    @Mock private ExpertProfileService expertProfileService;
    private ProfilServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfilServiceImpl(userRepository, startupRepository,
            expertProfileService, new ProfilMapper());
    }

    @Test
    void mettreAJour_client_modifieUserEtStartup() {
        User u = User.builder().id(5L).firstName("C").lastName("L")
            .email("cli@x").userType(UserType.CLIENT).build();
        Startup s = Startup.builder().id(7L).user(u).identifiantUnique("cli@x").actif(true).build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(u));
        when(startupRepository.findByUserId(5L)).thenReturn(Optional.of(s));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProfilRequest req = UpdateProfilRequest.builder()
            .firstName("Clara").domaineActivite("SaaS").nombreEmployes(20).build();

        var res = service.mettreAJour(5L, req);

        assertThat(u.getFirstName()).isEqualTo("Clara");
        assertThat(s.getDomaineActivite()).isEqualTo("SaaS");
        assertThat(s.getNombreEmployes()).isEqualTo(20);
        assertThat(res.getUserType()).isEqualTo("CLIENT");
        assertThat(res.getDomaineActivite()).isEqualTo("SaaS");
    }
}
