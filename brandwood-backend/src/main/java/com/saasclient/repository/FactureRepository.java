package com.saasclient.repository;

import com.saasclient.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByExpertIdOrderByDateEmissionDesc(Long expertId);

    List<Facture> findByStartup_User_IdOrderByDateEmissionDesc(Long userId);

    boolean existsByNumero(String numero);

    long countByExpertId(Long expertId);
}
