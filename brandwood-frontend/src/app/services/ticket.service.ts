import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Message, StatutTicket, Ticket, TicketRequest } from '../models/ticket.model';

export interface TicketIa {
  prioriteSuggeree: string; categorie: string; sentiment: string;
  resume: string; reponseSuggeree: string; source: string;
}

@Injectable({ providedIn: 'root' })
export class TicketService {

  private readonly api = `${environment.apiUrl}/tickets`;

  constructor(private http: HttpClient) {}

  analyser(id: number): Observable<TicketIa> {
    return this.http.post<TicketIa>(`${this.api}/${id}/ia/analyser`, {});
  }

  lister(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.api);
  }

  obtenir(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.api}/${id}`);
  }

  creer(req: TicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(this.api, req);
  }

  changerStatut(id: number, statut: StatutTicket): Observable<Ticket> {
    return this.http.patch<Ticket>(`${this.api}/${id}/statut`, { statut });
  }

  ajouterMessage(id: number, contenu: string): Observable<Message> {
    return this.http.post<Message>(`${this.api}/${id}/messages`, { contenu });
  }
}
