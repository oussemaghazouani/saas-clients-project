import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FactureService } from '../../services/facture.service';
import { Facture } from '../../models/facture.model';

@Component({
  selector: 'app-facture-detail',
  templateUrl: './facture-detail.component.html'
})
export class FactureDetailComponent implements OnInit {

  facture: Facture | null = null;
  loading = false;
  error = '';
  private id!: number;

  constructor(private route: ActivatedRoute, private svc: FactureService) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.svc.obtenir(this.id).subscribe({
      next: f => { this.facture = f; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Facture introuvable.'; this.loading = false; }
    });
  }

  imprimer(): void { window.print(); }
}
