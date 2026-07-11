import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { RapportService } from '../../services/rapport.service';
import { RapportProjet } from '../../models/projet.model';

@Component({
  selector: 'app-rapport',
  templateUrl: './rapport.component.html'
})
export class RapportComponent implements OnInit {

  rapport: RapportProjet | null = null;
  loading = false;
  error = '';
  private id!: number;

  constructor(private route: ActivatedRoute, private rapportSvc: RapportService) {}

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.rapportSvc.getRapport(this.id).subscribe({
      next: r => { this.rapport = r; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Rapport indisponible.'; this.loading = false; }
    });
  }

  imprimer(): void { window.print(); }

  taux(): number {
    return this.rapport && this.rapport.tachesTotal > 0
      ? Math.round(100 * this.rapport.tachesTerminees / this.rapport.tachesTotal) : 0;
  }
}
