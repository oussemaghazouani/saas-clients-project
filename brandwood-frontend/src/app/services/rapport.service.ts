import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RapportProjet } from '../models/projet.model';

@Injectable({ providedIn: 'root' })
export class RapportService {

  constructor(private http: HttpClient) {}

  getRapport(projetId: number): Observable<RapportProjet> {
    return this.http.get<RapportProjet>(`${environment.apiUrl}/projets/${projetId}/rapport`);
  }
}
