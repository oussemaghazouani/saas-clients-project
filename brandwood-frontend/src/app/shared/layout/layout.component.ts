import { Component, Input } from '@angular/core';
import { AuthService, AuthResponse } from '../../services/auth.service';

interface NavItem { label: string; icon: string; route: string; }

/** Layout applicatif réutilisable (sidebar role-aware + topbar) pour les écrans du Module 2. */
@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html'
})
export class LayoutComponent {

  @Input() title = '';

  user: AuthResponse | null;
  navItems: NavItem[] = [];

  constructor(private authService: AuthService) {
    this.user = this.authService.getCurrentUser();
    this.navItems = this.buildNavItems();
  }

  private buildNavItems(): NavItem[] {
    const dashboard: NavItem = { label: 'Tableau de bord', icon: 'bi-speedometer2', route: '/dashboard' };
    switch (this.user?.userType) {
      case 'SUPER_ADMIN':
        return [
          dashboard,
          { label: 'Statistiques', icon: 'bi-graph-up', route: '/statistiques' },
          { label: 'Messagerie',   icon: 'bi-chat-dots',    route: '/messagerie' },
          { label: 'Prestataires', icon: 'bi-people',      route: '/admin/prestataires' },
          { label: 'Packs',        icon: 'bi-box-seam',     route: '/admin/packs' },
          { label: 'Abonnements',  icon: 'bi-credit-card',  route: '/admin/abonnements' }
        ];
      case 'PRESTATAIRE':
        return [
          dashboard,
          { label: 'Statistiques', icon: 'bi-graph-up', route: '/statistiques' },
          { label: 'Messagerie',   icon: 'bi-chat-dots',    route: '/messagerie' },
          { label: 'Projets',     icon: 'bi-kanban',      route: '/projets' },
          { label: 'Mes clients', icon: 'bi-people',      route: '/prestataire/clients' },
          { label: 'Campagnes',   icon: 'bi-megaphone',   route: '/campagnes' },
          { label: 'Support',     icon: 'bi-headset',     route: '/support' },
          { label: 'Facturation', icon: 'bi-receipt',     route: '/factures' },
          { label: 'Mon profil',  icon: 'bi-person-gear', route: '/profil' }
        ];
      default:
        return [
          dashboard,
          { label: 'Statistiques', icon: 'bi-graph-up', route: '/statistiques' },
          { label: 'Messagerie',   icon: 'bi-chat-dots',    route: '/messagerie' },
          { label: 'Projets',    icon: 'bi-kanban',      route: '/projets' },
          { label: 'Campagnes',  icon: 'bi-megaphone',   route: '/campagnes' },
          { label: 'Support',    icon: 'bi-headset',     route: '/support' },
          { label: 'Facturation', icon: 'bi-receipt',    route: '/factures' },
          { label: 'Mon profil', icon: 'bi-person-gear', route: '/profil' }
        ];
    }
  }

  get userTypeLabel(): string {
    switch (this.user?.userType) {
      case 'CLIENT':      return 'Client';
      case 'PRESTATAIRE': return 'Prestataire';
      case 'SUPER_ADMIN': return 'Administrateur';
      default:            return '';
    }
  }

  get userTypeBadgeClass(): string {
    switch (this.user?.userType) {
      case 'CLIENT':      return 'badge-available';
      case 'PRESTATAIRE': return 'badge-category';
      case 'SUPER_ADMIN': return 'badge-admin';
      default:            return 'badge-pending';
    }
  }

  logout(): void { this.authService.logout(); }
}
