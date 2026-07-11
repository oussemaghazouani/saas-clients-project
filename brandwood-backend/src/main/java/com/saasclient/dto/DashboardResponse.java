package com.saasclient.dto;

import lombok.*;

import java.util.List;

/** Données agrégées du tableau de bord, adaptées au rôle de l'utilisateur. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DashboardResponse {
    private String userType;
    private String prenom;
    private List<KpiDto> kpis;
    private List<ProjetResponse> projetsRecents;
}
