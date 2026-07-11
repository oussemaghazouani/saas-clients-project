import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Guard "invité" : empeche un utilisateur deja connecte d'acceder
 * aux pages login / register (il est redirige vers le dashboard).
 */
@Injectable({ providedIn: 'root' })
export class GuestGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean | UrlTree {
    if (this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/dashboard']);
    }
    return true;
  }
}
