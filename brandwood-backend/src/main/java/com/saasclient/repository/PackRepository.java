package com.saasclient.repository;

import com.saasclient.entity.Pack;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackRepository extends JpaRepository<Pack, Long> {

    boolean existsByNomIgnoreCase(String nom);

    Page<Pack> findByActif(boolean actif, Pageable pageable);

    List<Pack> findByActifTrue();
}
