package com.saasclient.repository;

import com.saasclient.entity.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReunionRepository extends JpaRepository<Reunion, Long> {

    List<Reunion> findByProjetIdOrderByDateHeureAsc(Long projetId);

    List<Reunion> findByProjet_ExpertIdOrderByDateHeureAsc(Long expertId);

    List<Reunion> findByProjet_Startup_User_IdOrderByDateHeureAsc(Long userId);

    long countByProjetId(Long projetId);
}
