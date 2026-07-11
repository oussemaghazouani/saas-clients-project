import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../services/admin.service';
import { ActionStatut, Prestataire, StatutCompte } from '../../models/saas.model';

@Component({
  selector: 'app-admin-prestataires',
  templateUrl: './admin-prestataires.component.html'
})
export class AdminPrestatairesComponent implements OnInit {

  prestataires: Prestataire[] = [];
  loading = false;
  error = '';
  message = '';
  recherche = '';
  filtreStatut: StatutCompte | '' = '';
  statuts: StatutCompte[] = ['ACTIF', 'DESACTIVE', 'SUSPENDU', 'SUPPRIME'];

  constructor(private admin: AdminService) {}

  ngOnInit(): void { this.charger(); }

  get prestatairesAffiches(): Prestataire[] {
    const q = this.recherche.trim().toLowerCase();
    if (!q) { return this.prestataires; }
    return this.prestataires.filter(p =>
      (p.firstName + ' ' + p.lastName).toLowerCase().includes(q) ||
      (p.email || '').toLowerCase().includes(q));
  }

  initiales(p: Prestataire): string {
    return ((p.firstName?.charAt(0) || '') + (p.lastName?.charAt(0) || '')).toUpperCase();
  }

  charger(): void {
    this.loading = true;
    this.error = '';
    this.admin.listPrestataires(this.filtreStatut || undefined).subscribe({
      next: p => { this.prestataires = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  action(p: Prestataire, a: ActionStatut): void {
    this.admin.changerStatut(p.id, a).subscribe({
      next: up => { Object.assign(p, up); this.flash('Statut mis à jour : ' + up.statutCompte); },
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  supprimer(p: Prestataire): void {
    if (!confirm(`Supprimer définitivement ${p.firstName} ${p.lastName} ? Action irréversible.`)) return;
    this.admin.supprimerPrestataire(p.id).subscribe({
      next: () => { this.prestataires = this.prestataires.filter(x => x.id !== p.id); this.flash('Prestataire supprimé.'); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  badge(s: StatutCompte): string {
    return { ACTIF: 'badge-available', DESACTIVE: 'badge-pending', SUSPENDU: 'badge-admin', SUPPRIME: 'badge-admin' }[s];
  }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
