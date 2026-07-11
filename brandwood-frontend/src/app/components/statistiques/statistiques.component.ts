import { Component, OnInit } from '@angular/core';
import { ChartData, DataPoint, InsightsData, StatistiquesData, StatistiquesService } from '../../services/statistiques.service';

@Component({
  selector: 'app-statistiques',
  templateUrl: './statistiques.component.html'
})
export class StatistiquesComponent implements OnInit {

  data: StatistiquesData | null = null;
  loading = false;
  error = '';

  insights: InsightsData | null = null;
  loadingInsights = false;

  constructor(private svc: StatistiquesService) {}

  ngOnInit(): void { this.charger(); }

  charger(): void {
    this.loading = true;
    this.svc.charger().subscribe({
      next: d => { this.data = d; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  analyserIa(): void {
    this.loadingInsights = true;
    this.svc.insights().subscribe({
      next: i => { this.insights = i; this.loadingInsights = false; },
      error: () => { this.loadingInsights = false; }
    });
  }

  total(points: DataPoint[]): number { return points.reduce((s, p) => s + p.valeur, 0); }

  max(points: DataPoint[]): number { return Math.max(1, ...points.map(p => p.valeur)); }

  pct(p: DataPoint, points: DataPoint[]): number {
    const t = this.total(points);
    return t > 0 ? Math.round(p.valeur / t * 100) : 0;
  }

  /** Construit le dégradé conique du donut à partir des points. */
  donut(points: DataPoint[]): string {
    const t = this.total(points) || 1;
    let acc = 0;
    const segs: string[] = [];
    for (const p of points) {
      const start = acc / t * 100;
      acc += p.valeur;
      const end = acc / t * 100;
      segs.push(`${p.couleur} ${start}% ${end}%`);
    }
    return `conic-gradient(${segs.join(', ')})`;
  }
}
