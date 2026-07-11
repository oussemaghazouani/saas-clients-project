import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CampagneService, CampagneIaResponse } from '../../services/campagne.service';
import { PrestataireService } from '../../services/prestataire.service';
import { AuthService } from '../../services/auth.service';
import { Campagne, CanalCampagne, StatutCampagne } from '../../models/campagne.model';
import { Client } from '../../models/saas.model';

@Component({
  selector: 'app-campagnes',
  templateUrl: './campagnes.component.html'
})
export class CampagnesComponent implements OnInit {

  campagnes: Campagne[] = [];
  clients: Client[] = [];
  loading = false;
  error = '';
  message = '';
  isPrestataire = false;
  showForm = false;
  editingId: number | null = null;
  form!: FormGroup;

  canaux: CanalCampagne[] = ['EMAIL', 'RESEAUX_SOCIAUX', 'SEO', 'SEA', 'EVENEMENT', 'AUTRE'];
  statuts: StatutCampagne[] = ['BROUILLON', 'PLANIFIEE', 'EN_COURS', 'TERMINEE', 'ANNULEE'];

  // IA — génération de contenu
  iaLoading = false;
  iaResult: CampagneIaResponse | null = null;

  constructor(
    private campagneSvc: CampagneService,
    private presta: PrestataireService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.isPrestataire = this.auth.getUserType() === 'PRESTATAIRE';
    this.buildForm();
    this.charger();
    if (this.isPrestataire) {
      this.presta.mesClients(0, 100).subscribe({ next: p => this.clients = p.content, error: () => {} });
    }
  }

  private buildForm(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(200)]],
      canal: ['EMAIL', Validators.required],
      statut: ['BROUILLON'],
      startupId: [null, Validators.required],
      budget: [null],
      dateDebut: [null],
      dateFin: [null],
      impressions: [0],
      clics: [0],
      conversions: [0],
      description: ['']
    });
  }

  charger(): void {
    this.loading = true;
    this.campagneSvc.lister().subscribe({
      next: p => { this.campagnes = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  nouveau(): void { this.editingId = null; this.buildForm(); this.iaResult = null; this.showForm = true; }

  /** Génère le contenu de la campagne par IA à partir d'un thème, puis pré-remplit le formulaire. */
  genererIa(theme: string): void {
    if (!theme.trim()) return;
    this.iaLoading = true;
    this.campagneSvc.genererParIa({ theme: theme.trim(), canal: this.form.value.canal }).subscribe({
      next: r => {
        this.iaResult = r;
        this.form.patchValue({
          nom: r.nom,
          canal: r.canalRecommande || this.form.value.canal,
          description: r.accroche + '\n\n' + r.description
            + (r.hashtags?.length ? '\n\n' + r.hashtags.map(h => '#' + h).join(' ') : '')
        });
        this.iaLoading = false;
      },
      error: e => { this.error = e.error?.message || 'Génération IA impossible.'; this.iaLoading = false; }
    });
  }

  editer(c: Campagne): void { this.editingId = c.id; this.form.patchValue(c); this.showForm = true; }

  annuler(): void { this.showForm = false; }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const req = this.form.value;
    const obs = this.editingId ? this.campagneSvc.modifier(this.editingId, req) : this.campagneSvc.creer(req);
    obs.subscribe({
      next: () => { this.flash(this.editingId ? 'Campagne modifiée.' : 'Campagne créée.'); this.showForm = false; this.charger(); },
      error: e => this.error = e.error?.message || 'Enregistrement impossible.'
    });
  }

  supprimer(c: Campagne): void {
    if (!confirm(`Supprimer la campagne « ${c.nom} » ?`)) return;
    this.campagneSvc.supprimer(c.id).subscribe({
      next: () => { this.flash('Campagne supprimée.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  statutBadge(s: string): string {
    return { BROUILLON: 'status-planifie', PLANIFIEE: 'status-encours', EN_COURS: 'status-tests', TERMINEE: 'status-termine', ANNULEE: 'badge-admin' }[s] || 'status-planifie';
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
