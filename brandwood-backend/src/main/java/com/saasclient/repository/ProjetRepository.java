package com.saasclient.repository;

import com.saasclient.entity.Projet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjetRepository extends JpaRepository<Projet, Long> {

    Page<Projet> findByExpertId(Long expertId, Pageable pageable);

    Page<Projet> findByStartup_User_Id(Long userId, Pageable pageable);

    long countByExpertId(Long expertId);

    long countByStartup_User_Id(Long userId);

    long countByExpertIdAndStatut(Long expertId, com.saasclient.entity.StatutProjet statut);
}
