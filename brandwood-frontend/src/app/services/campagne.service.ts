import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from '../models/saas.model';
import { Campagne, CampagneRequest } from '../models/campagne.model';

export interface CampagneIaRequest { theme: string; canal?: string; cible?: string; }
export interface CampagneIaResponse {
  nom: string; accroche: string; description: string; audience: string;
  canalRecommande: string; hashtags: string[]; source: string;
}

@Injectable({ providedIn: 'root' })
export class CampagneService {

  private readonly api = `${environment.apiUrl}/campagnes`;

  constructor(private http: HttpClient) {}

  genererParIa(req: CampagneIaRequest): Observable<CampagneIaResponse> {
    return this.http.post<CampagneIaResponse>(`${this.api}/ia/generer`, req);
  }

  lister(page = 0, size = 50): Observable<Page<Campagne>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<Campagne>>(this.api, { params });
  }

  creer(req: CampagneRequest): Observable<Campagne> {
    return this.http.post<Campagne>(this.api, req);
  }

  modifier(id: number, req: CampagneRequest): Observable<Campagne> {
    return this.http.put<Campagne>(`${this.api}/${id}`, req);
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
