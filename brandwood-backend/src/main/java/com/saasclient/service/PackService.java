package com.saasclient.service;

import com.saasclient.dto.PackRequest;
import com.saasclient.dto.PackResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PackService {

    Page<PackResponse> lister(Boolean actif, Pageable pageable);

    /** Catalogue des packs actifs — accessible à tout utilisateur authentifié. */
    List<PackResponse> listerActifs();

    PackResponse obtenir(Long id);

    PackResponse creer(PackRequest req);

    PackResponse modifier(Long id, PackRequest req);

    void supprimer(Long id);
}
