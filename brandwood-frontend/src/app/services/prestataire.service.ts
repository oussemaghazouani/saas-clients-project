import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Abonnement, Client, Pack, Page, ProvisionClientRequest, ProvisionClientResponse, SouscriptionRequest
} from '../models/saas.model';

/** Opérations réservées au PRESTATAIRE : provisioning clients et abonnements. */
@Injectable({ providedIn: 'root' })
export class PrestataireService {

  private readonly api = `${environment.apiUrl}/prestataire`;

  constructor(private http: HttpClient) {}

  // ── Clients ────────────────────────────────────────────────────────────────
  provisionner(req: ProvisionClientRequest): Observable<ProvisionClientResponse> {
    return this.http.post<ProvisionClientResponse>(`${this.api}/clients`, req);
  }

  mesClients(page = 0, size = 20): Observable<Page<Client>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Client>>(`${this.api}/clients`, { params });
  }

  desactiver(id: number): Observable<Client> {
    return this.http.patch<Client>(`${this.api}/clients/${id}/desactiver`, {});
  }

  reactiver(id: number): Observable<Client> {
    return this.http.patch<Client>(`${this.api}/clients/${id}/reactiver`, {});
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/clients/${id}`);
  }

  // ── Abonnements ────────────────────────────────────────────────────────────
  souscrire(req: SouscriptionRequest): Observable<Abonnement> {
    return this.http.post<Abonnement>(`${this.api}/abonnements`, req);
  }

  mesAbonnements(): Observable<Abonnement[]> {
    return this.http.get<Abonnement[]>(`${this.api}/abonnements`);
  }

  /** Catalogue des packs actifs (accessible à tout utilisateur authentifié). */
  cataloguePacks(): Observable<Pack[]> {
    return this.http.get<Pack[]>(`${environment.apiUrl}/packs`);
  }
}
