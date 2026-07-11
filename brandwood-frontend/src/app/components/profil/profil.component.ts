import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProfilService } from '../../services/profil.service';
import { PrestataireService } from '../../services/prestataire.service';
import { Abonnement, Pack, Profil, UpdateProfilRequest } from '../../models/saas.model';

@Component({
  selector: 'app-profil',
  templateUrl: './profil.component.html'
})
export class ProfilComponent implements OnInit {

  profil: Profil | null = null;
  loading = false;
  saving = false;
  error = '';
  message = '';
  form!: FormGroup;
  competencesText = '';

  // Souscription (prestataire)
  packs: Pack[] = [];
  abonnements: Abonnement[] = [];
  selectedPackId: number | null = null;
  souscribing = false;

  constructor(
    private profilSvc: ProfilService,
    private presta: PrestataireService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void { this.charger(); }

  get isPrestataire(): boolean { return this.profil?.userType === 'PRESTATAIRE'; }
  get isClient(): boolean { return this.profil?.userType === 'CLIENT'; }

  charger(): void {
    this.loading = true;
    this.profilSvc.monProfil().subscribe({
      next: p => {
        this.profil = p;
        this.build(p);
        this.loading = false;
        if (p.userType === 'PRESTATAIRE') { this.loadAbonnements(); }
      },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  private build(p: Profil): void {
    this.form = this.fb.group({
      firstName: [p.firstName, Validators.maxLength(100)],
      lastName: [p.lastName, Validators.maxLength(100)],
      phone: [p.phone || ''],
      specialite: [p.specialite || ''],
      tarifHoraire: [p.tarifHoraire ?? null],
      disponibilite: [p.disponibilite ?? true],
      domaineActivite: [p.domaineActivite || ''],
      siret: [p.siret || ''],
      adresse: [p.adresse || ''],
      nombreEmployes: [p.nombreEmployes ?? null]
    });
    this.competencesText = (p.competences || []).join(', ');
  }

  private loadAbonnements(): void {
    this.presta.cataloguePacks().subscribe({ next: p => this.packs = p, error: () => {} });
    this.presta.mesAbonnements().subscribe({ next: a => this.abonnements = a, error: () => {} });
  }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error = '';
    const v = this.form.value;
    const req: UpdateProfilRequest = { firstName: v.firstName, lastName: v.lastName, phone: v.phone };
    if (this.isPrestataire) {
      req.specialite = v.specialite;
      req.tarifHoraire = v.tarifHoraire;
      req.disponibilite = v.disponibilite;
      req.competences = this.competencesText.split(',').map(s => s.trim()).filter(s => s.length > 0);
    }
    if (this.isClient) {
      req.domaineActivite = v.domaineActivite;
      req.siret = v.siret;
      req.adresse = v.adresse;
      req.nombreEmployes = v.nombreEmployes;
    }
    this.profilSvc.mettreAJour(req).subscribe({
      next: p => { this.profil = p; this.saving = false; this.flash('Profil mis à jour.'); },
      error: e => { this.saving = false; this.error = e.error?.message || 'Mise à jour impossible.'; }
    });
  }

  souscrire(): void {
    if (!this.selectedPackId) { this.error = 'Sélectionnez un pack.'; return; }
    this.souscribing = true;
    this.error = '';
    this.presta.souscrire({ packId: this.selectedPackId, methodePaiement: 'VIREMENT' }).subscribe({
      next: a => {
        this.abonnements.unshift(a);
        this.souscribing = false;
        this.flash('Demande envoyée. En attente de validation du virement par l\'administrateur.');
      },
      error: e => { this.souscribing = false; this.error = e.error?.message || 'Souscription impossible.'; }
    });
  }

  badge(s: string): string {
    return { EN_ATTENTE: 'badge-pending', ACTIF: 'badge-available', EXPIRE: 'badge-admin', ANNULE: 'badge-admin' }[s] || 'badge-pending';
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
