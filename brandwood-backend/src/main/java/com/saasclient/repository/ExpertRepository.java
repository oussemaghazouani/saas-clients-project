package com.saasclient.repository;

import com.saasclient.entity.Expert;
import com.saasclient.entity.StatutCompte;
import com.saasclient.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertRepository extends JpaRepository<Expert, Long> {

    Optional<Expert> findByUserId(Long userId);

    Optional<Expert> findByUser(User user);

    boolean existsByUserId(Long userId);

    Page<Expert> findByStatutCompte(StatutCompte statutCompte, Pageable pageable);

    long countByStatutCompte(StatutCompte statutCompte);
}
