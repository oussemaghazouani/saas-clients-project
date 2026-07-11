package com.saasclient.service;

import com.saasclient.dto.ClientResponse;
import com.saasclient.dto.ProvisionClientRequest;
import com.saasclient.dto.ProvisionClientResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClientService {

    ProvisionClientResponse provisionner(Long prestataireUserId, ProvisionClientRequest req);

    Page<ClientResponse> listerMesClients(Long prestataireUserId, Pageable pageable);

    ClientResponse desactiver(Long prestataireUserId, Long startupId);

    ClientResponse reactiver(Long prestataireUserId, Long startupId);

    void supprimer(Long prestataireUserId, Long startupId);
}
