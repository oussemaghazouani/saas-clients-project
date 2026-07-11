package com.saasclient.repository;

import com.saasclient.entity.StatutTache;
import com.saasclient.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByProjetIdOrderByPositionAsc(Long projetId);

    long countByProjetId(Long projetId);

    long countByProjetIdAndStatut(Long projetId, StatutTache statut);
}
