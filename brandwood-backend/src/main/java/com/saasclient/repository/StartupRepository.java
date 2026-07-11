package com.saasclient.repository;

import com.saasclient.entity.Startup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StartupRepository extends JpaRepository<Startup, Long> {

    Optional<Startup> findByUserId(Long userId);

    boolean existsByIdentifiantUnique(String identifiantUnique);

    Page<Startup> findByPrestataireId(Long prestataireId, Pageable pageable);

    List<Startup> findByPrestataireId(Long prestataireId);

    long countByPrestataireIdAndActifTrue(Long prestataireId);
}
