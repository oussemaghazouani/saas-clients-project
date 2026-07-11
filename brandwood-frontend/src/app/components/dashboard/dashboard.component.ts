import { Component, OnInit } from '@angular/core';
import { AuthService, AuthResponse } from '../../services/auth.service';
import { DashboardService, Kpi } from '../../services/dashboard.service';
import { Projet } from '../../models/projet.model';

interface NavItem { label: string; icon: string; route: string; active: boolean; }

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {

  user: AuthResponse | null = null;
  navItems: NavItem[] = [];
  kpis: Kpi[] = [];
  projetsRecents: Projet[] = [];
  loading = false;

  lastLoginText = new Date().toLocaleDateString('fr-FR', {
    day: '2-digit', month: 'long', hour: '2-digit', minute: '2-digit'
  });

  constructor(private authService: AuthService, private dashboardSvc: DashboardService) {}

  ngOnInit(): void {
    this.user = this.authService.getCurrentUser();
    this.navItems = this.buildNavItems();
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.dashboardSvc.charger().subscribe({
      next: d => { this.kpis = d.kpis || []; this.projetsRecents = d.projetsRecents || []; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get quickLinks(): NavItem[] { return this.navItems.filter(i => i.route !== '/dashboard'); }

  logout(): void { this.authService.logout(); }

  get greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Bonjour';
    if (h < 18) return 'Bon après-midi';
    return 'Bonsoir';
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

  statutBadge(s: string): string {
    return { PLANIFIE: 'status-planifie', EN_COURS: 'status-encours', EN_PAUSE: 'status-tests', TERMINE: 'status-termine', ANNULE: 'badge-admin' }[s] || 'status-planifie';
  }

  private buildNavItems(): NavItem[] {
    const items: NavItem[] = [
      { label: 'Tableau de bord', icon: 'bi-speedometer2', route: '/dashboard', active: true },
      { label: 'Statistiques', icon: 'bi-graph-up', route: '/statistiques', active: false },
      { label: 'Messagerie', icon: 'bi-chat-dots', route: '/messagerie', active: false }
    ];
    switch (this.user?.userType) {
      case 'SUPER_ADMIN':
        items.push(
          { label: 'Prestataires', icon: 'bi-people',     route: '/admin/prestataires', active: false },
          { label: 'Packs',        icon: 'bi-box-seam',    route: '/admin/packs',        active: false },
          { label: 'Abonnements',  icon: 'bi-credit-card', route: '/admin/abonnements',  active: false }
        );
        break;
      case 'PRESTATAIRE':
        items.push(
          { label: 'Projets',     icon: 'bi-kanban',      route: '/projets',             active: false },
          { label: 'Mes clients', icon: 'bi-people',      route: '/prestataire/clients', active: false },
          { label: 'Campagnes',   icon: 'bi-megaphone',   route: '/campagnes',           active: false },
          { label: 'Support',     icon: 'bi-headset',     route: '/support',             active: false },
          { label: 'Facturation', icon: 'bi-receipt',     route: '/factures',            active: false },
          { label: 'Mon profil',  icon: 'bi-person-gear', route: '/profil',              active: false }
        );
        break;
      case 'CLIENT':
        items.push(
          { label: 'Projets',    icon: 'bi-kanban',      route: '/projets',   active: false },
          { label: 'Campagnes',  icon: 'bi-megaphone',   route: '/campagnes', active: false },
          { label: 'Support',    icon: 'bi-headset',     route: '/support',   active: false },
          { label: 'Facturation', icon: 'bi-receipt',    route: '/factures',  active: false },
          { label: 'Mon profil', icon: 'bi-person-gear', route: '/profil',    active: false }
        );
        break;
    }
    return items;
  }
}
