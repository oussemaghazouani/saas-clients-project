package com.saasclient.dto;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChartData {
    /** "donut" ou "bar". */
    private String type;
    private String titre;
    private List<DataPoint> points;
}
