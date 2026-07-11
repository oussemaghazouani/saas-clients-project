import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Profil, UpdateProfilRequest } from '../models/saas.model';

/** Profil propre de l'utilisateur connecté (CLIENT ou PRESTATAIRE). */
@Injectable({ providedIn: 'root' })
export class ProfilService {

  private readonly api = `${environment.apiUrl}/profil`;

  constructor(private http: HttpClient) {}

  monProfil(): Observable<Profil> {
    return this.http.get<Profil>(this.api);
  }

  mettreAJour(req: UpdateProfilRequest): Observable<Profil> {
    return this.http.put<Profil>(this.api, req);
  }
}
