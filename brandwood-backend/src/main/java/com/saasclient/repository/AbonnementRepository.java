package com.saasclient.repository;

import com.saasclient.entity.Abonnement;
import com.saasclient.entity.StatutAbonnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByExpertIdOrderByCreatedAtDesc(Long expertId);

    List<Abonnement> findByExpertIdAndStatut(Long expertId, StatutAbonnement statut);

    Page<Abonnement> findByStatut(StatutAbonnement statut, Pageable pageable);

    boolean existsByExpertIdAndStatut(Long expertId, StatutAbonnement statut);

    long countByStatut(StatutAbonnement statut);

    long countByExpertId(Long expertId);

    void deleteByExpertId(Long expertId);
}
