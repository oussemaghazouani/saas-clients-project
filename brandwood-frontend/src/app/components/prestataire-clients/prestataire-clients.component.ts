import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PrestataireService } from '../../services/prestataire.service';
import { Client } from '../../models/saas.model';

@Component({
  selector: 'app-prestataire-clients',
  templateUrl: './prestataire-clients.component.html'
})
export class PrestataireClientsComponent implements OnInit {

  clients: Client[] = [];
  loading = false;
  error = '';
  message = '';
  showForm = false;
  submitting = false;
  dernierIdentifiant = '';
  form!: FormGroup;

  constructor(private presta: PrestataireService, private fb: FormBuilder) {}

  ngOnInit(): void { this.buildForm(); this.charger(); }

  private buildForm(): void {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      phone: [''],
      password: ['', [Validators.required, Validators.minLength(8),
        Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$')]],
      domaineActivite: [''],
      siret: [''],
      adresse: [''],
      nombreEmployes: [null]
    });
  }

  charger(): void {
    this.loading = true;
    this.presta.mesClients().subscribe({
      next: p => { this.clients = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  nouveau(): void { this.buildForm(); this.dernierIdentifiant = ''; this.showForm = true; }

  annuler(): void { this.showForm = false; }

  provisionner(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.submitting = true;
    this.error = '';
    this.presta.provisionner(this.form.value).subscribe({
      next: r => {
        this.submitting = false;
        this.showForm = false;
        this.dernierIdentifiant = r.identifiantUnique;
        this.flash('Client provisionné avec succès.');
        this.charger();
      },
      error: e => { this.submitting = false; this.error = e.error?.message || 'Provisioning impossible.'; }
    });
  }

  toggle(c: Client): void {
    const obs = c.actif ? this.presta.desactiver(c.id) : this.presta.reactiver(c.id);
    obs.subscribe({
      next: up => { Object.assign(c, up); this.flash(up.actif ? 'Client réactivé.' : 'Client désactivé.'); },
      error: e => this.error = e.error?.message || 'Action impossible.'
    });
  }

  supprimer(c: Client): void {
    if (!confirm(`Supprimer définitivement ${c.firstName} ${c.lastName} ? (suppression en cascade)`)) return;
    this.presta.supprimer(c.id).subscribe({
      next: () => { this.clients = this.clients.filter(x => x.id !== c.id); this.flash('Client supprimé.'); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  copier(txt: string): void {
    navigator.clipboard?.writeText(txt);
    this.flash('Identifiant copié.');
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 4000); }
}
