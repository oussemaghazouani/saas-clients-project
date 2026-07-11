package com.saasclient.repository;

import com.saasclient.entity.Membre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembreRepository extends JpaRepository<Membre, Long> {

    List<Membre> findByProjetIdOrderByCreatedAtAsc(Long projetId);

    long countByProjetId(Long projetId);
}
