import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree
} from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Guard qui protege les routes necessitant une authentification.
 * Redirige vers /login si l'utilisateur n'est pas connecte.
 */
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {

    if (this.authService.isLoggedIn()) {
      // Verification optionnelle du role requis (usage : canActivate: [AuthGuard], data: { role: 'ROLE_CLIENT' })
      const requiredRole: string | undefined = route.data?.['role'];
      if (requiredRole && !this.authService.hasRole(requiredRole)) {
        return this.router.createUrlTree(['/dashboard']);
      }
      return true;
    }

    // Non connecte : redirection vers login avec returnUrl
    return this.router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url }
    });
  }
}
