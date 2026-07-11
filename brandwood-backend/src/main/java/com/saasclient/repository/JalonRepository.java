package com.saasclient.repository;

import com.saasclient.entity.Jalon;
import com.saasclient.entity.StatutJalon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JalonRepository extends JpaRepository<Jalon, Long> {

    List<Jalon> findByProjetIdOrderByDatePrevueAsc(Long projetId);

    long countByProjetId(Long projetId);

    long countByProjetIdAndStatut(Long projetId, StatutJalon statut);
}
