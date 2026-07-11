import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProjetService } from '../../services/projet.service';
import { AuthService } from '../../services/auth.service';
import { AnalyseRisque, Jalon, Membre, MembreRequest, Projet, Reunion, RoleProjet, StatutJalon, StatutTache, Tache } from '../../models/projet.model';

@Component({
  selector: 'app-projet-detail',
  templateUrl: './projet-detail.component.html'
})
export class ProjetDetailComponent implements OnInit {

  projet: Projet | null = null;
  jalons: Jalon[] = [];
  loading = false;
  error = '';
  message = '';
  isPrestataire = false;
  activeTab: 'apercu' | 'jalons' | 'taches' | 'membres' | 'reunions' | 'activite' = 'apercu';
  taches: Tache[] = [];
  risque: AnalyseRisque | null = null;
  colonnes = [
    { label: 'À faire',     statut: 'A_FAIRE' },
    { label: 'En cours',    statut: 'EN_COURS' },
    { label: 'En révision', statut: 'EN_REVISION' },
    { label: 'Terminé',     statut: 'TERMINE' }
  ];
  showJalonForm = false;
  jalonForm!: FormGroup;
  membres: Membre[] = [];
  showMembreForm = false;
  membreForm!: FormGroup;
  roles: RoleProjet[] = ['CHEF_PROJET', 'DEVELOPPEUR', 'DESIGNER', 'TESTEUR', 'ANALYSTE'];
  reunions: Reunion[] = [];
  showReunionForm = false;
  reunionForm!: FormGroup;
  private id!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projetSvc: ProjetService,
    private auth: AuthService,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.isPrestataire = this.auth.getUserType() === 'PRESTATAIRE';
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.jalonForm = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(150)]],
      datePrevue: [null],
      description: ['']
    });
    this.membreForm = this.fb.group({
      prenom: ['', [Validators.required, Validators.maxLength(100)]],
      nom: ['', [Validators.required, Validators.maxLength(100)]],
      email: [''],
      roleProjet: ['DEVELOPPEUR', Validators.required]
    });
    this.reunionForm = this.fb.group({
      titre: ['', [Validators.required, Validators.maxLength(200)]],
      dateHeure: ['', Validators.required],
      dureeMinutes: [60],
      lien: [''],
      description: ['']
    });
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.projetSvc.obtenir(this.id).subscribe({
      next: p => { this.projet = p; this.chargerJalons(); this.chargerMembres(); this.chargerTaches(); this.chargerReunions(); this.loading = false; },
      error: e => { this.error = e.error?.message || 'Projet introuvable.'; this.loading = false; }
    });
  }

  private chargerJalons(): void {
    this.projetSvc.jalons(this.id).subscribe({ next: j => this.jalons = j, error: () => {} });
  }

  ajouterJalon(): void {
    if (this.jalonForm.invalid) { this.jalonForm.markAllAsTouched(); return; }
    this.projetSvc.ajouterJalon(this.id, this.jalonForm.value).subscribe({
      next: () => { this.showJalonForm = false; this.jalonForm.reset(); this.flash('Jalon ajouté.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Ajout impossible.'
    });
  }

  toggleJalon(j: Jalon): void {
    const nouveau: StatutJalon = j.statut === 'ATTEINT' ? 'A_VENIR' : 'ATTEINT';
    this.projetSvc.changerStatutJalon(j.id, nouveau).subscribe({
      next: () => { this.flash('Jalon mis à jour.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  supprimerJalon(j: Jalon): void {
    if (!confirm(`Supprimer le jalon « ${j.nom} » ?`)) return;
    this.projetSvc.supprimerJalon(j.id).subscribe({
      next: () => { this.flash('Jalon supprimé.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  private chargerMembres(): void {
    this.projetSvc.membres(this.id).subscribe({ next: m => this.membres = m, error: () => {} });
  }

  ajouterMembre(): void {
    if (this.membreForm.invalid) { this.membreForm.markAllAsTouched(); return; }
    this.projetSvc.ajouterMembre(this.id, this.membreForm.value as MembreRequest).subscribe({
      next: () => {
        this.showMembreForm = false;
        this.membreForm.reset({ roleProjet: 'DEVELOPPEUR' });
        this.flash('Membre ajouté.');
        this.charger();
      },
      error: e => this.error = e.error?.message || 'Ajout impossible.'
    });
  }

  supprimerMembre(m: Membre): void {
    if (!confirm(`Retirer ${m.prenom} ${m.nom} de l'équipe ?`)) return;
    this.projetSvc.supprimerMembre(m.id).subscribe({
      next: () => { this.flash('Membre retiré.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  private chargerTaches(): void {
    this.projetSvc.taches(this.id).subscribe({ next: t => this.taches = t, error: () => {} });
  }

  tachesParStatut(statut: string): Tache[] {
    return this.taches.filter(t => t.statut === statut);
  }

  prioriteBadge(p: string): string {
    return { BASSE: 'status-planifie', MOYENNE: 'badge-category', HAUTE: 'status-tests', CRITIQUE: 'badge-admin' }[p] || 'badge-category';
  }

  genererTaches(): void {
    this.projetSvc.genererTaches(this.id).subscribe({
      next: r => { this.flash(r.message); this.chargerTaches(); },
      error: e => this.error = e.error?.message || 'Génération impossible.'
    });
  }

  analyserRisque(): void {
    this.projetSvc.analyserRisque(this.id).subscribe({
      next: r => this.risque = r,
      error: e => this.error = e.error?.message || 'Analyse impossible.'
    });
  }

  deplacerTache(t: Tache, sens: number): void {
    const ordre = ['A_FAIRE', 'EN_COURS', 'EN_REVISION', 'TERMINE'];
    const i = ordre.indexOf(t.statut);
    const ni = Math.max(0, Math.min(3, i + sens));
    if (ni === i) return;
    this.projetSvc.changerStatutTache(t.id, ordre[ni] as StatutTache).subscribe({
      next: () => this.chargerTaches(),
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  supprimerTache(t: Tache): void {
    if (!confirm(`Supprimer la tâche « ${t.titre} » ?`)) return;
    this.projetSvc.supprimerTache(t.id).subscribe({
      next: () => { this.flash('Tâche supprimée.'); this.chargerTaches(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  private chargerReunions(): void {
    this.projetSvc.reunions(this.id).subscribe({ next: r => this.reunions = r, error: () => {} });
  }

  ajouterReunion(): void {
    if (this.reunionForm.invalid) { this.reunionForm.markAllAsTouched(); return; }
    this.projetSvc.creerReunion(this.id, this.reunionForm.value).subscribe({
      next: () => {
        this.showReunionForm = false;
        this.reunionForm.reset({ dureeMinutes: 60 });
        this.flash('Réunion planifiée.');
        this.chargerReunions();
      },
      error: e => this.error = e.error?.message || 'Création impossible.'
    });
  }

  supprimerReunion(r: Reunion): void {
    if (!confirm(`Supprimer la réunion « ${r.titre} » ?`)) return;
    this.projetSvc.supprimerReunion(r.id).subscribe({
      next: () => { this.flash('Réunion supprimée.'); this.chargerReunions(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  supprimerProjet(): void {
    if (!this.projet || !confirm(`Supprimer le projet « ${this.projet.nom} » ?`)) return;
    this.projetSvc.supprimer(this.id).subscribe({
      next: () => this.router.navigate(['/projets']),
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  jalonBadge(s: string): string {
    return { A_VENIR: 'status-planifie', ATTEINT: 'status-termine', EN_RETARD: 'badge-admin' }[s] || 'status-planifie';
  }

  statutBadge(s: string): string {
    return { PLANIFIE: 'status-planifie', EN_COURS: 'status-encours', EN_PAUSE: 'status-tests', TERMINE: 'status-termine', ANNULE: 'badge-admin' }[s] || 'status-planifie';
  }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
