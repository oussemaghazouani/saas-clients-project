import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Projet } from '../models/projet.model';

export interface Kpi {
  label: string;
  valeur: string;
  sousLabel: string;
  icone: string;
  couleur: string;
}

export interface DashboardData {
  userType: string;
  prenom: string;
  kpis: Kpi[];
  projetsRecents: Projet[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {

  private readonly api = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  charger(): Observable<DashboardData> {
    return this.http.get<DashboardData>(this.api);
  }
}
