import { Injectable } from '@angular/core';
import {
  HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Intercepteur HTTP :
 * - Ajoute l'en-tete Authorization: Bearer <token> sur toutes les requetes
 *   a destination de l'API.
 * - Redirige vers /login en cas de 401 (token expire / invalide).
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = this.authService.getToken();

    // On n'ajoute le token que sur les requetes API.
    const isApiUrl = req.url.includes('/api/');

    if (token && isApiUrl) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token expire ou invalide : on deconnecte et on renvoie vers login.
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}
