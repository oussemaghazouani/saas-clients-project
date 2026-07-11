import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TicketService, TicketIa } from '../../services/ticket.service';
import { AuthService } from '../../services/auth.service';
import { PrioriteTicket, StatutTicket, Ticket } from '../../models/ticket.model';

@Component({
  selector: 'app-support',
  templateUrl: './support.component.html'
})
export class SupportComponent implements OnInit {

  tickets: Ticket[] = [];
  selected: Ticket | null = null;
  loading = false;
  error = '';
  message = '';
  isClient = false;
  isPrestataire = false;
  showForm = false;
  form!: FormGroup;
  nouveauMessage = '';
  priorites: PrioriteTicket[] = ['BASSE', 'MOYENNE', 'HAUTE', 'URGENTE'];
  statuts: StatutTicket[] = ['OUVERT', 'EN_COURS', 'RESOLU', 'FERME'];

  // IA — analyse du ticket
  ia: TicketIa | null = null;
  iaLoading = false;

  constructor(private svc: TicketService, private auth: AuthService, private fb: FormBuilder) {}

  ngOnInit(): void {
    const t = this.auth.getUserType();
    this.isClient = t === 'CLIENT';
    this.isPrestataire = t === 'PRESTATAIRE';
    this.form = this.fb.group({
      sujet: ['', [Validators.required, Validators.maxLength(200)]],
      description: [''],
      priorite: ['MOYENNE']
    });
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.svc.lister().subscribe({
      next: t => { this.tickets = t; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  ouvrir(id: number): void {
    this.showForm = false;
    this.ia = null;
    this.svc.obtenir(id).subscribe({
      next: t => this.selected = t,
      error: e => this.error = e.error?.message || 'Ticket introuvable.'
    });
  }

  /** Analyse IA du ticket sélectionné (priorité, catégorie, sentiment, réponse suggérée). */
  analyserIa(): void {
    if (!this.selected) { return; }
    this.iaLoading = true;
    this.svc.analyser(this.selected.id).subscribe({
      next: r => { this.ia = r; this.iaLoading = false; },
      error: e => { this.error = e.error?.message || 'Analyse IA impossible.'; this.iaLoading = false; }
    });
  }

  /** Insère la réponse suggérée par l'IA dans la zone de saisie. */
  insererReponse(): void {
    if (this.ia) { this.nouveauMessage = this.ia.reponseSuggeree; }
  }

  nouveau(): void { this.showForm = true; this.selected = null; this.form.reset({ priorite: 'MOYENNE' }); }

  creer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.svc.creer(this.form.value).subscribe({
      next: t => { this.showForm = false; this.flash('Ticket créé.'); this.charger(); this.ouvrir(t.id); },
      error: e => this.error = e.error?.message || 'Création impossible.'
    });
  }

  envoyer(): void {
    const c = this.nouveauMessage.trim();
    if (!c || !this.selected) { return; }
    const id = this.selected.id;
    this.svc.ajouterMessage(id, c).subscribe({
      next: () => { this.nouveauMessage = ''; this.ouvrir(id); this.charger(); },
      error: e => this.error = e.error?.message || 'Envoi impossible.'
    });
  }

  changerStatut(s: StatutTicket): void {
    if (!this.selected) { return; }
    const id = this.selected.id;
    this.svc.changerStatut(id, s).subscribe({
      next: () => { this.flash('Statut mis à jour.'); this.ouvrir(id); this.charger(); },
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  badge(s: string): string {
    return { OUVERT: 'status-encours', EN_COURS: 'status-tests', RESOLU: 'status-termine', FERME: 'badge-pending' }[s] || 'status-planifie';
  }

  prioBadge(p: string): string {
    return { BASSE: 'status-planifie', MOYENNE: 'badge-category', HAUTE: 'status-tests', URGENTE: 'badge-admin' }[p] || 'badge-category';
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
