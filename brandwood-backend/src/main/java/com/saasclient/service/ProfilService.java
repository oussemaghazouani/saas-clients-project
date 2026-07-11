package com.saasclient.service;

import com.saasclient.dto.ProfilResponse;
import com.saasclient.dto.UpdateProfilRequest;

public interface ProfilService {

    ProfilResponse monProfil(Long userId);

    ProfilResponse mettreAJour(Long userId, UpdateProfilRequest req);
}
