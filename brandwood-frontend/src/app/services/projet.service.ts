import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from '../models/saas.model';
import {
  AnalyseRisque, GenerationTaches, Jalon, JalonRequest, Membre, MembreRequest,
  Projet, ProjetRequest, Reunion, ReunionRequest, StatutJalon, StatutTache, Tache, TacheRequest
} from '../models/projet.model';

/** Projets IT et jalons (CLIENT en lecture, PRESTATAIRE en écriture). */
@Injectable({ providedIn: 'root' })
export class ProjetService {

  private readonly api = `${environment.apiUrl}/projets`;
  private readonly jalonsApi = `${environment.apiUrl}/jalons`;
  private readonly tachesApi = `${environment.apiUrl}/taches`;
  private readonly reunionsApi = `${environment.apiUrl}/reunions`;

  constructor(private http: HttpClient) {}

  // ── Projets ──
  lister(page = 0, size = 50): Observable<Page<Projet>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Projet>>(this.api, { params });
  }

  obtenir(id: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.api}/${id}`);
  }

  creer(req: ProjetRequest): Observable<Projet> {
    return this.http.post<Projet>(this.api, req);
  }

  modifier(id: number, req: ProjetRequest): Observable<Projet> {
    return this.http.put<Projet>(`${this.api}/${id}`, req);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }

  // ── Jalons ──
  jalons(projetId: number): Observable<Jalon[]> {
    return this.http.get<Jalon[]>(`${this.api}/${projetId}/jalons`);
  }

  ajouterJalon(projetId: number, req: JalonRequest): Observable<Jalon> {
    return this.http.post<Jalon>(`${this.api}/${projetId}/jalons`, req);
  }

  modifierJalon(jalonId: number, req: JalonRequest): Observable<Jalon> {
    return this.http.put<Jalon>(`${this.jalonsApi}/${jalonId}`, req);
  }

  changerStatutJalon(jalonId: number, statut: StatutJalon): Observable<Jalon> {
    return this.http.patch<Jalon>(`${this.jalonsApi}/${jalonId}/statut`, { statut });
  }

  supprimerJalon(jalonId: number): Observable<void> {
    return this.http.delete<void>(`${this.jalonsApi}/${jalonId}`);
  }

  // ── Membres (équipe) ──
  membres(projetId: number): Observable<Membre[]> {
    return this.http.get<Membre[]>(`${this.api}/${projetId}/membres`);
  }

  ajouterMembre(projetId: number, req: MembreRequest): Observable<Membre> {
    return this.http.post<Membre>(`${this.api}/${projetId}/membres`, req);
  }

  supprimerMembre(membreId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/membres/${membreId}`);
  }

  // ── Tâches & Kanban (Module 5) ──
  taches(projetId: number): Observable<Tache[]> {
    return this.http.get<Tache[]>(`${this.api}/${projetId}/taches`);
  }

  creerTache(projetId: number, req: TacheRequest): Observable<Tache> {
    return this.http.post<Tache>(`${this.api}/${projetId}/taches`, req);
  }

  changerStatutTache(tacheId: number, statut: StatutTache): Observable<Tache> {
    return this.http.patch<Tache>(`${this.tachesApi}/${tacheId}/statut`, { statut });
  }

  supprimerTache(tacheId: number): Observable<void> {
    return this.http.delete<void>(`${this.tachesApi}/${tacheId}`);
  }

  // ── IA ──
  genererTaches(projetId: number): Observable<GenerationTaches> {
    return this.http.post<GenerationTaches>(`${this.api}/${projetId}/taches/generer`, {});
  }

  analyserRisque(projetId: number): Observable<AnalyseRisque> {
    return this.http.get<AnalyseRisque>(`${this.api}/${projetId}/risque`);
  }

  // ── Réunions (Module 6) ──
  reunions(projetId: number): Observable<Reunion[]> {
    return this.http.get<Reunion[]>(`${this.api}/${projetId}/reunions`);
  }

  creerReunion(projetId: number, req: ReunionRequest): Observable<Reunion> {
    return this.http.post<Reunion>(`${this.api}/${projetId}/reunions`, req);
  }

  supprimerReunion(reunionId: number): Observable<void> {
    return this.http.delete<void>(`${this.reunionsApi}/${reunionId}`);
  }
}
