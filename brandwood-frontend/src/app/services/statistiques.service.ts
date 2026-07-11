import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Kpi { label: string; valeur: string; sousLabel: string; icone: string; couleur: string; }
export interface DataPoint { label: string; valeur: number; couleur: string; }
export interface ChartData { type: string; titre: string; points: DataPoint[]; }
export interface StatistiquesData { userType: string; kpis: Kpi[]; charts: ChartData[]; }
export interface InsightsData { insights: string[]; recommandation: string; source: string; }

@Injectable({ providedIn: 'root' })
export class StatistiquesService {

  private readonly api = `${environment.apiUrl}/statistiques`;

  constructor(private http: HttpClient) {}

  charger(): Observable<StatistiquesData> {
    return this.http.get<StatistiquesData>(this.api);
  }

  insights(): Observable<InsightsData> {
    return this.http.get<InsightsData>(`${this.api}/ia/insights`);
  }
}
