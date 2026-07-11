import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './guards/auth.guard';
import { GuestGuard } from './guards/guest.guard';

const routes: Routes = [
  // Redirect racine
  { path: '', redirectTo: '/login', pathMatch: 'full' },

  // Pages publiques (invites uniquement)
  {
    path: 'login',
    canActivate: [GuestGuard],
    loadChildren: () =>
      import('./components/login/login.module').then(m => m.LoginModule)
  },
  {
    path: 'register/client',
    canActivate: [GuestGuard],
    loadChildren: () =>
      import('./components/register-client/register-client.module').then(m => m.RegisterClientModule)
  },
  {
    path: 'register/prestataire',
    canActivate: [GuestGuard],
    loadChildren: () =>
      import('./components/register-prestataire/register-prestataire.module').then(m => m.RegisterPrestataireModule)
  },
  {
    path: 'verify-email',
    loadChildren: () =>
      import('./components/verify-email/verify-email.module').then(m => m.VerifyEmailModule)
  },
  {
    path: 'forgot-password',
    canActivate: [GuestGuard],
    loadChildren: () =>
      import('./components/forgot-password/forgot-password.module').then(m => m.ForgotPasswordModule)
  },
  {
    path: 'reset-password',
    canActivate: [GuestGuard],
    loadChildren: () =>
      import('./components/reset-password/reset-password.module').then(m => m.ResetPasswordModule)
  },

  // Pages protegees
  {
    path: 'dashboard',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/dashboard/dashboard.module').then(m => m.DashboardModule)
  },

  // ── Module 2 : Gestion des Utilisateurs, Acces & Packs ──
  {
    path: 'admin/prestataires',
    canActivate: [AuthGuard],
    data: { role: 'ROLE_SUPER_ADMIN' },
    loadChildren: () =>
      import('./components/admin-prestataires/admin-prestataires.module').then(m => m.AdminPrestatairesModule)
  },
  {
    path: 'admin/packs',
    canActivate: [AuthGuard],
    data: { role: 'ROLE_SUPER_ADMIN' },
    loadChildren: () =>
      import('./components/admin-packs/admin-packs.module').then(m => m.AdminPacksModule)
  },
  {
    path: 'admin/abonnements',
    canActivate: [AuthGuard],
    data: { role: 'ROLE_SUPER_ADMIN' },
    loadChildren: () =>
      import('./components/admin-abonnements/admin-abonnements.module').then(m => m.AdminAbonnementsModule)
  },
  {
    path: 'prestataire/clients',
    canActivate: [AuthGuard],
    data: { role: 'ROLE_PRESTATAIRE' },
    loadChildren: () =>
      import('./components/prestataire-clients/prestataire-clients.module').then(m => m.PrestataireClientsModule)
  },
  {
    path: 'profil',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/profil/profil.module').then(m => m.ProfilModule)
  },
  {
    path: 'campagnes',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/campagnes/campagnes.module').then(m => m.CampagnesModule)
  },

  // ── Module 3 : Projets IT ──
  {
    path: 'projets',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/projets/projets.module').then(m => m.ProjetsModule)
  },
  {
    path: 'projets/:id/rapport',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/rapport/rapport.module').then(m => m.RapportModule)
  },
  {
    path: 'projets/:id',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/projet-detail/projet-detail.module').then(m => m.ProjetDetailModule)
  },

  {
    path: 'factures',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/factures/factures.module').then(m => m.FacturesModule)
  },
  {
    path: 'factures/:id',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/facture-detail/facture-detail.module').then(m => m.FactureDetailModule)
  },
  {
    path: 'support',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/support/support.module').then(m => m.SupportModule)
  },

  {
    path: 'statistiques',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/statistiques/statistiques.module').then(m => m.StatistiquesModule)
  },

  {
    path: 'messagerie',
    canActivate: [AuthGuard],
    loadChildren: () =>
      import('./components/messagerie/messagerie.module').then(m => m.MessagerieModule)
  },

  // Fallback
  { path: '**', redirectTo: '/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
