import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Facture, FactureRequest, StatutFacture } from '../models/facture.model';

@Injectable({ providedIn: 'root' })
export class FactureService {

  private readonly api = `${environment.apiUrl}/factures`;

  constructor(private http: HttpClient) {}

  lister(): Observable<Facture[]> {
    return this.http.get<Facture[]>(this.api);
  }

  obtenir(id: number): Observable<Facture> {
    return this.http.get<Facture>(`${this.api}/${id}`);
  }

  creer(req: FactureRequest): Observable<Facture> {
    return this.http.post<Facture>(this.api, req);
  }

  changerStatut(id: number, statut: StatutFacture): Observable<Facture> {
    return this.http.patch<Facture>(`${this.api}/${id}/statut`, { statut });
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
