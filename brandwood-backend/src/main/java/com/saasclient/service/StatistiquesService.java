package com.saasclient.service;

import com.saasclient.dto.InsightsResponse;
import com.saasclient.dto.StatistiquesResponse;

public interface StatistiquesService {

    StatistiquesResponse charger(Long userId);

    /** Analyse IA en langage naturel des indicateurs de l'utilisateur. */
    InsightsResponse insights(Long userId);
}
