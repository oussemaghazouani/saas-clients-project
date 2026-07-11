package com.saasclient.service;

import com.saasclient.dto.DashboardResponse;

public interface DashboardService {

    /** Construit le tableau de bord (KPI + projets récents) selon le rôle de l'utilisateur. */
    DashboardResponse charger(Long userId);
}
