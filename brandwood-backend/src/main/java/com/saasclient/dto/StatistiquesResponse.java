package com.saasclient.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatistiquesResponse {
    private String userType;
    private List<KpiDto> kpis;
    private List<ChartData> charts;
}
