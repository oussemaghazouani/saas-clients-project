package com.saasclient.repository;

import com.saasclient.entity.MessageTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageTicketRepository extends JpaRepository<MessageTicket, Long> {

    List<MessageTicket> findByTicketIdOrderByDateEnvoiAsc(Long ticketId);

    long countByTicketId(Long ticketId);
}
