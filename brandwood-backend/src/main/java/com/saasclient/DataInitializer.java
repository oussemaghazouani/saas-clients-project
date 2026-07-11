package com.saasclient;

import com.saasclient.entity.Role;
import com.saasclient.entity.User;
import com.saasclient.entity.UserType;
import com.saasclient.repository.RoleRepository;
import com.saasclient.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private static final String ADMIN_EMAIL = "admin@saas.co";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        createRoleIfNotExists("ROLE_CLIENT",      "Client de la plateforme");
        createRoleIfNotExists("ROLE_PRESTATAIRE", "Prestataire de services");
        createRoleIfNotExists("ROLE_SUPER_ADMIN", "Administrateur global");

        if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
            Role adminRole = roleRepository.findByName("ROLE_SUPER_ADMIN").orElseThrow();
            User admin = User.builder()
                .firstName("Super")
                .lastName("Admin")
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode("Admin@123"))
                .userType(UserType.SUPER_ADMIN)
                .enabled(true)
                .roles(Set.of(adminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            userRepository.save(admin);
            log.info("Super admin créé : {} / Admin@123", ADMIN_EMAIL);
        }
    }

    private void createRoleIfNotExists(String name, String description) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().name(name).description(description).build());
        }
    }
}
