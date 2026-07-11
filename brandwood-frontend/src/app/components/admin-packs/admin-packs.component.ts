import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { Pack } from '../../models/saas.model';

@Component({
  selector: 'app-admin-packs',
  templateUrl: './admin-packs.component.html'
})
export class AdminPacksComponent implements OnInit {

  packs: Pack[] = [];
  loading = false;
  error = '';
  message = '';
  showForm = false;
  editingId: number | null = null;
  form!: FormGroup;

  constructor(private admin: AdminService, private fb: FormBuilder) {}

  ngOnInit(): void { this.buildForm(); this.charger(); }

  private buildForm(): void {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      prix: [0, [Validators.required, Validators.min(0)]],
      nbProjetsMax: [1, [Validators.required, Validators.min(1)]],
      nbClientsMax: [1, [Validators.required, Validators.min(1)]],
      dureeMois: [1, [Validators.required, Validators.min(1)]],
      actif: [true]
    });
  }

  charger(): void {
    this.loading = true;
    this.admin.listPacks().subscribe({
      next: p => { this.packs = p.content; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  nouveau(): void { this.editingId = null; this.buildForm(); this.showForm = true; }

  editer(p: Pack): void { this.editingId = p.id; this.form.patchValue(p); this.showForm = true; }

  annuler(): void { this.showForm = false; }

  enregistrer(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const req = this.form.value;
    const obs = this.editingId
      ? this.admin.modifierPack(this.editingId, req)
      : this.admin.creerPack(req);
    obs.subscribe({
      next: () => { this.flash(this.editingId ? 'Pack modifié.' : 'Pack créé.'); this.showForm = false; this.charger(); },
      error: e => this.error = e.error?.message || 'Enregistrement impossible.'
    });
  }

  supprimer(p: Pack): void {
    if (!confirm(`Supprimer le pack « ${p.nom} » ?`)) return;
    this.admin.supprimerPack(p.id).subscribe({
      next: () => { this.flash('Pack supprimé.'); this.charger(); },
      error: e => this.error = e.error?.message || 'Suppression impossible.'
    });
  }

  get f() { return this.form.controls; }

  private flash(m: string): void { this.message = m; setTimeout(() => this.message = '', 3000); }
}
