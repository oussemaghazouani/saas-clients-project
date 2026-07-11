package com.saasclient.repository;

import com.saasclient.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByStartup_User_IdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByExpertIdOrderByCreatedAtDesc(Long expertId);
}
