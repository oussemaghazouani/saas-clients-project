import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Abonnement, ActionStatut, Page, Pack, PackRequest, Prestataire,
  StatutAbonnement, StatutCompte
} from '../models/saas.model';

/** Opérations réservées au SUPER_ADMIN : prestataires, packs, abonnements. */
@Injectable({ providedIn: 'root' })
export class AdminService {

  private readonly api = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // ── Prestataires ───────────────────────────────────────────────────────────
  listPrestataires(statut?: StatutCompte, page = 0, size = 20): Observable<Page<Prestataire>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (statut) params = params.set('statut', statut);
    return this.http.get<Page<Prestataire>>(`${this.api}/prestataires`, { params });
  }

  changerStatut(id: number, action: ActionStatut): Observable<Prestataire> {
    return this.http.patch<Prestataire>(`${this.api}/prestataires/${id}/statut`, { action });
  }

  supprimerPrestataire(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/prestataires/${id}`);
  }

  // ── Packs ──────────────────────────────────────────────────────────────────
  listPacks(actif?: boolean, page = 0, size = 50): Observable<Page<Pack>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (actif !== undefined && actif !== null) params = params.set('actif', actif);
    return this.http.get<Page<Pack>>(`${this.api}/packs`, { params });
  }

  creerPack(req: PackRequest): Observable<Pack> {
    return this.http.post<Pack>(`${this.api}/packs`, req);
  }

  modifierPack(id: number, req: PackRequest): Observable<Pack> {
    return this.http.put<Pack>(`${this.api}/packs/${id}`, req);
  }

  supprimerPack(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/packs/${id}`);
  }

  // ── Abonnements ────────────────────────────────────────────────────────────
  listAbonnements(statut?: StatutAbonnement, page = 0, size = 20): Observable<Page<Abonnement>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (statut) params = params.set('statut', statut);
    return this.http.get<Page<Abonnement>>(`${this.api}/abonnements`, { params });
  }

  validerAbonnement(id: number): Observable<Abonnement> {
    return this.http.patch<Abonnement>(`${this.api}/abonnements/${id}/valider`, {});
  }
}
