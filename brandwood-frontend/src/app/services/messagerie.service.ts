import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

export interface Contact {
  userId: number;
  nom: string;
  role: string;
  dernierMessage: string | null;
  dernierMessageDate: string | null;
  nonLus: number;
}

export interface MessagePrive {
  id: number;
  expediteurId: number;
  expediteurNom: string;
  destinataireId: number;
  contenu: string;
  lu: boolean;
  dateEnvoi: string;
}

@Injectable({ providedIn: 'root' })
export class MessagerieService {

  private readonly api = `${environment.apiUrl}/messagerie`;

  /** Flux des messages recus en temps reel via WebSocket. */
  readonly messages$ = new Subject<MessagePrive>();

  private socket: WebSocket | null = null;
  private reconnectTimer: any = null;
  private voulu = false; // connexion souhaitee (pour gerer la reconnexion)

  constructor(private http: HttpClient, private auth: AuthService) {}

  // ── REST ───────────────────────────────────────────────────────────────────
  contacts(): Observable<Contact[]> { return this.http.get<Contact[]>(`${this.api}/contacts`); }

  fil(autreUserId: number): Observable<MessagePrive[]> {
    return this.http.get<MessagePrive[]>(`${this.api}/conversations/${autreUserId}`);
  }

  envoyer(destinataireId: number, contenu: string): Observable<MessagePrive> {
    return this.http.post<MessagePrive>(`${this.api}/messages`, { destinataireId, contenu });
  }

  nonLus(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.api}/non-lus`);
  }

  // ── WebSocket temps reel ─────────────────────────────────────────────────────
  connecter(): void {
    this.voulu = true;
    const token = this.auth.getToken();
    if (!token || (this.socket && this.socket.readyState <= WebSocket.OPEN)) { return; }

    const proto = window.location.protocol === 'https:' ? 'wss' : 'ws';
    const url = `${proto}://${window.location.host}/ws/messagerie?token=${encodeURIComponent(token)}`;
    this.socket = new WebSocket(url);

    this.socket.onmessage = (evt) => {
      try { this.messages$.next(JSON.parse(evt.data) as MessagePrive); } catch { /* ignore */ }
    };
    this.socket.onclose = () => {
      this.socket = null;
      if (this.voulu) { this.planifierReconnexion(); }
    };
    this.socket.onerror = () => { this.socket?.close(); };
  }

  deconnecter(): void {
    this.voulu = false;
    if (this.reconnectTimer) { clearTimeout(this.reconnectTimer); this.reconnectTimer = null; }
    this.socket?.close();
    this.socket = null;
  }

  private planifierReconnexion(): void {
    if (this.reconnectTimer) { return; }
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      if (this.voulu) { this.connecter(); }
    }, 3000);
  }
}
