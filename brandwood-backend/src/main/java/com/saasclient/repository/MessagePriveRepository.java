package com.saasclient.repository;

import com.saasclient.entity.MessagePrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessagePriveRepository extends JpaRepository<MessagePrive, Long> {

    /** Fil complet entre deux utilisateurs, ordre chronologique. */
    @Query("SELECT m FROM MessagePrive m WHERE (m.expediteur.id = :a AND m.destinataire.id = :b) "
        + "OR (m.expediteur.id = :b AND m.destinataire.id = :a) ORDER BY m.createdAt ASC")
    List<MessagePrive> fil(@Param("a") Long a, @Param("b") Long b);

    /** Tous les messages impliquant l'utilisateur (pour construire la liste des conversations). */
    @Query("SELECT m FROM MessagePrive m WHERE m.expediteur.id = :userId OR m.destinataire.id = :userId "
        + "ORDER BY m.createdAt DESC")
    List<MessagePrive> impliquant(@Param("userId") Long userId);

    long countByDestinataireIdAndLuFalse(Long destinataireId);

    long countByExpediteurIdAndDestinataireIdAndLuFalse(Long expediteurId, Long destinataireId);

    @Modifying
    @Query("UPDATE MessagePrive m SET m.lu = true WHERE m.destinataire.id = :me AND m.expediteur.id = :autre AND m.lu = false")
    void marquerLus(@Param("me") Long me, @Param("autre") Long autre);
}
