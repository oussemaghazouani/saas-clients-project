import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatResponse {
  reply: string;
  source: string;
}

@Injectable({ providedIn: 'root' })
export class AssistantService {

  private readonly api = `${environment.apiUrl}/assistant`;

  constructor(private http: HttpClient) {}

  chat(message: string): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.api}/chat`, { message });
  }
}
