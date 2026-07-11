import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface NotificationItem {
  id: number;
  titre: string;
  message?: string;
  lien?: string;
  lu: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private readonly api = `${environment.apiUrl}/notifications`;

  constructor(private http: HttpClient) {}

  lister(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(this.api);
  }

  count(): Observable<{ nonLues: number }> {
    return this.http.get<{ nonLues: number }>(`${this.api}/count`);
  }

  marquerLu(id: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/lu`, {});
  }

  marquerToutLu(): Observable<void> {
    return this.http.patch<void>(`${this.api}/lu-tout`, {});
  }
}
