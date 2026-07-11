import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { FactureService } from '../../services/facture.service';
import { PrestataireService } from '../../services/prestataire.service';
import { ProjetService } from '../../services/projet.service';
import { AuthService } from '../../services/auth.service';
import { Facture, StatutFacture } from '../../models/facture.model';
import { Client } from '../../models/saas.model';
import { Projet } from '../../models/projet.model';

@Component({
  selector: 'app-factures',
  templateUrl: './factures.component.html'
})
export class FacturesComponent implements OnInit {

  factures: Facture[] = [];
  clients: Client[] = [];
  projets: Projet[] = [];
  loading = false;
  error = '';
  message = '';
  isPrestataire = false;
  showForm = false;
  form!: FormGroup;
  statuts: StatutFacture[] = ['BROUILLON', 'ENVOYEE', 'PAYEE', 'ANNULEE'];

  constructor(
    private svc: FactureService,
    private presta: PrestataireService,
    private projetSvc: ProjetService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.isPrestataire = this.auth.getUserType() === 'PRESTATAIRE';
    this.buildForm();
    this.charger();
    if (this.isPrestataire) {
      this.presta.mesClients().subscribe({ next: p => this.clients = p.content, error: () => {} });
      this.projetSvc.lister().subscribe({ next: p => this.projets = p.content, error: () => {} });
    }
  }

  private buildForm(): void {
    this.form = this.fb.group({
      startupId: [null, Validators.required],
      projetId: [null],
      tauxTva: [20],
      dateEcheance: [null],
      lignes: this.fb.array([this.ligneGroup()])
    });
  }

  private ligneGroup(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      quantite: [1, [Validators.required, Validators.min(0.01)]],
      prixUnitaire: [0, [Validators.required, Validators.min(0)]]
    });
  }

  get lignes(): FormArray { return this.form.get('lignes') as FormArray; }

  ajouterLigne(): void { this.lignes.push(this.ligneGroup()); }
  retirerLigne(i: number): void { if (this.lignes.length > 1) { this.lignes.removeAt(i); } }

  get totalHt(): number {
    return this.lignes.controls.reduce((s, c) => s + (c.value.quantite || 0) * (c.value.prixUnitaire || 0), 0);
  }
  get totalTtc(): number { return this.totalHt * (1 + (this.form.value.tauxTva || 0) / 100); }

  charger(): void {
    this.loading = true;
    this.svc.lister().subscribe({
      next: f => { this.factures = f; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  nouveau(): void { this.buildForm(); this.showForm = true; }

  creer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.svc.creer(this.form.value).subscribe({
      next: () => { this.showForm = false; this.flash('Facture créée (brouillon).'); this.charger(); },
      error: e => this.error = e.error?.message || 'Création impossible.'
    });
  }

  changerStatut(f: Facture, s: StatutFacture): void {
    this.svc.changerStatut(f.id, s).subscribe({
      next: up => { Object.assign(f, up); this.flash('Statut : ' + s); },
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  supprimer(f: Facture): void {
    if (!confirm(`Supprimer la facture ${f.numero} ?`)) { return; }
    this.svc.supprimer(f.id).subscribe({
      next: () => { this.factures = this.factures.filter(x => x.id !== f.id); this.flash('Facture supprimée.'); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  badge(s: string): string {
    return { BROUILLON: 'badge-pending', ENVOYEE: 'status-encours', PAYEE: 'status-termine', ANNULEE: 'badge-admin' }[s] || 'badge-pending';
  }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
