package com.saasclient.repository;

import com.saasclient.entity.Campagne;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampagneRepository extends JpaRepository<Campagne, Long> {

    Page<Campagne> findByExpertId(Long expertId, Pageable pageable);

    Page<Campagne> findByStartup_User_Id(Long userId, Pageable pageable);

    long countByExpertId(Long expertId);
}
