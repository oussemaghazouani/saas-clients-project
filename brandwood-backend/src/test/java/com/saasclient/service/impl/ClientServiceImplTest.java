package com.saasclient.service.impl;

import com.saasclient.dto.ProvisionClientRequest;
import com.saasclient.entity.*;
import com.saasclient.exception.AccessForbiddenException;
import com.saasclient.exception.BusinessRuleException;
import com.saasclient.mapper.ClientMapper;
import com.saasclient.repository.RoleRepository;
import com.saasclient.repository.StartupRepository;
import com.saasclient.repository.UserRepository;
import com.saasclient.service.ExpertProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock private ExpertProfileService expertProfileService;
    @Mock private StartupRepository startupRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    private ClientServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClientServiceImpl(expertProfileService, startupRepository,
            userRepository, roleRepository, passwordEncoder, new ClientMapper());
    }

    private Expert expert(StatutCompte statut, Pack pack) {
        User u = User.builder().id(1L).firstName("Paul").lastName("Presta")
            .email("p@x.co").userType(UserType.PRESTATAIRE).build();
        return Expert.builder().id(10L).user(u).pack(pack).statutCompte(statut)
            .competences(new ArrayList<>()).build();
    }

    private ProvisionClientRequest req() {
        return ProvisionClientRequest.builder().firstName("Clara").lastName("Client")
            .password("Passw0rd").domaineActivite("E-commerce").build();
    }

    @Test
    void provisionner_okSousEssaiGratuit() {
        Expert e = expert(StatutCompte.ACTIF, null); // pas de pack -> essai gratuit (limite 1)
        when(expertProfileService.getOrCreate(1L)).thenReturn(e);
        when(startupRepository.countByPrestataireIdAndActifTrue(10L)).thenReturn(0L);
        when(startupRepository.existsByIdentifiantUnique(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("ROLE_CLIENT"))
            .thenReturn(Optional.of(Role.builder().id(1L).name("ROLE_CLIENT").build()));
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> { User u = i.getArgument(0); u.setId(5L); return u; });
        when(startupRepository.save(any(Startup.class))).thenAnswer(i -> { Startup s = i.getArgument(0); s.setId(7L); return s; });

        var res = service.provisionner(1L, req());

        assertThat(res.getStartupId()).isEqualTo(7L);
        assertThat(res.getIdentifiantUnique()).contains("@saas-client.app");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void provisionner_limiteEssaiAtteinte_leveException() {
        Expert e = expert(StatutCompte.ACTIF, null);
        when(expertProfileService.getOrCreate(1L)).thenReturn(e);
        when(startupRepository.countByPrestataireIdAndActifTrue(10L)).thenReturn(1L); // déjà 1 client

        assertThatThrownBy(() -> service.provisionner(1L, req()))
            .isInstanceOf(BusinessRuleException.class)
            .hasMessageContaining("essai gratuit");
        verify(userRepository, never()).save(any());
    }

    @Test
    void provisionner_compteNonActif_leveForbidden() {
        Expert e = expert(StatutCompte.SUSPENDU, null);
        when(expertProfileService.getOrCreate(1L)).thenReturn(e);

        assertThatThrownBy(() -> service.provisionner(1L, req()))
            .isInstanceOf(AccessForbiddenException.class);
    }
}
