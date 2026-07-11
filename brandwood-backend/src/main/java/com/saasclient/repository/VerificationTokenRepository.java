package com.saasclient.repository;

import com.saasclient.entity.User;
import com.saasclient.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findTopByCodeAndUsedFalseOrderByExpiryDateDesc(String code);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.user = :user AND vt.used = false")
    void invalidateAllByUser(User user);
}
