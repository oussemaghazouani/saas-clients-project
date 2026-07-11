import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProjetService } from '../../services/projet.service';
import { PrestataireService } from '../../services/prestataire.service';
import { AuthService } from '../../services/auth.service';
import { Projet } from '../../models/projet.model';
import { Client } from '../../models/saas.model';

@Component({
  selector: 'app-projets',
  templateUrl: './projets.component.html'
})
export class ProjetsComponent implements OnInit {

  projets: Projet[] = [];
  loading = false;
  error = '';
  message = '';
  isPrestataire = false;
  showForm = false;
  submitting = false;
  form!: FormGroup;
  clients: Client[] = [];
  statuts = ['PLANIFIE', 'EN_COURS', 'EN_PAUSE', 'TERMINE', 'ANNULE'];

  constructor(
    private projetSvc: ProjetService,
    private presta: PrestataireService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.isPrestataire = this.auth.getUserType() === 'PRESTATAIRE';
    this.buildForm();
    this.charger();
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(150)]],
      description: [''],
      startupId: [null, Validators.required],
      statut: ['PLANIFIE'],
      budget: [null],
      dateDebut: [null],
      dateFin: [null],
      technologies: ['']
    });
  }

  charger(): void {
    this.loading = true;
    this.projetSvc.lister().subscribe({
      next: p => { this.projets = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  nouveau(): void {
    this.buildForm();
    this.showForm = true;
    this.presta.mesClients().subscribe({ next: p => this.clients = p.content, error: () => {} });
  }

  annuler(): void { this.showForm = false; }

  creer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.error = '';
    const v = this.form.value;
    const req = {
      nom: v.nom,
      description: v.description,
      startupId: v.startupId,
      statut: v.statut,
      budget: v.budget,
      dateDebut: v.dateDebut || undefined,
      dateFin: v.dateFin || undefined,
      technologies: (v.technologies || '').split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0)
    };
    this.projetSvc.creer(req).subscribe({
      next: () => { this.submitting = false; this.showForm = false; this.flash('Projet créé.'); this.charger(); },
      error: e => { this.submitting = false; this.error = e.error?.message || 'Création impossible.'; }
    });
  }

  badge(s: string): string {
    return { PLANIFIE: 'status-planifie', EN_COURS: 'status-encours', EN_PAUSE: 'status-tests', TERMINE: 'status-termine', ANNULE: 'badge-admin' }[s] || 'status-planifie';
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
