import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

// ─── Interfaces ────────────────────────────────────────────────────────────────

export interface RegisterClientRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  companyName: string;
}

export interface RegisterPrestataireRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  phone?: string;
  jobTitle: string;
  companyName?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface VerifyCodeRequest {
  email: string;
  code: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface ApiResponse {
  success: boolean;
  message: string;
}

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  userType: 'CLIENT' | 'PRESTATAIRE' | 'SUPER_ADMIN';
  roles: string[];
}

// ─── Service ───────────────────────────────────────────────────────────────────

const TOKEN_KEY  = 'bw_token';
const USER_KEY   = 'bw_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly apiUrl = `${environment.apiUrl}/auth`;

  /** Etat courant de l'utilisateur connecte (null = deconnecte). */
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.loadUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}

  // ── Inscription ──────────────────────────────────────────────────────────────

  registerClient(req: RegisterClientRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/register/client`, req);
  }

  registerPrestataire(req: RegisterPrestataireRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/register/prestataire`, req);
  }

  // ── Verification d'email ─────────────────────────────────────────────────────

  verifyEmail(req: VerifyCodeRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/verify`, req);
  }

  resendCode(email: string): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/resend-code?email=${encodeURIComponent(email)}`, {});
  }

  // ── Connexion ────────────────────────────────────────────────────────────────

  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, req).pipe(
      tap((response: AuthResponse) => this.saveSession(response))
    );
  }

  // ── Mot de passe oublie ──────────────────────────────────────────────────────

  forgotPassword(req: ForgotPasswordRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/forgot-password`, req);
  }

  resetPassword(req: ResetPasswordRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.apiUrl}/reset-password`, req);
  }

  // ── Session ──────────────────────────────────────────────────────────────────

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: string): boolean {
    return this.getCurrentUser()?.roles?.includes(role) ?? false;
  }

  getUserType(): string | null {
    return this.getCurrentUser()?.userType ?? null;
  }

  // ── Helpers privés ───────────────────────────────────────────────────────────

  private saveSession(auth: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, auth.token);
    localStorage.setItem(USER_KEY, JSON.stringify(auth));
    this.currentUserSubject.next(auth);
  }

  private loadUser(): AuthResponse | null {
    try {
      const json = localStorage.getItem(USER_KEY);
      return json ? JSON.parse(json) : null;
    } catch {
      return null;
    }
  }
}
