import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { Abonnement, StatutAbonnement } from '../../models/saas.model';

@Component({
  selector: 'app-admin-abonnements',
  templateUrl: './admin-abonnements.component.html'
})
export class AdminAbonnementsComponent implements OnInit {

  abonnements: Abonnement[] = [];
  loading = false;
  error = '';
  message = '';
  filtreStatut: StatutAbonnement | '' = 'EN_ATTENTE';
  statuts: StatutAbonnement[] = ['EN_ATTENTE', 'ACTIF', 'EXPIRE', 'ANNULE'];

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading = true;
    this.error = '';
    this.admin.listAbonnements(this.filtreStatut || undefined).subscribe({
      next: p => { this.abonnements = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  valider(a: Abonnement): void {
    this.admin.validerAbonnement(a.id).subscribe({
      next: up => { Object.assign(a, up); this.flash('Virement validé : abonnement activé.'); },
      error: e => this.error = e.error?.message || 'Validation impossible.'
    });
  }

  badge(s: StatutAbonnement): string {
    return { EN_ATTENTE: 'badge-pending', ACTIF: 'badge-available', EXPIRE: 'badge-admin', ANNULE: 'badge-admin' }[s];
  }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
