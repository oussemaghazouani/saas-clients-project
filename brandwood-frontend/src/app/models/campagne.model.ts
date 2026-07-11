// Modèles du Module 7 — Campagnes marketing.

export type CanalCampagne = 'EMAIL' | 'RESEAUX_SOCIAUX' | 'SEO' | 'SEA' | 'EVENEMENT' | 'AUTRE';
export type StatutCampagne = 'BROUILLON' | 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';

export interface Campagne {
  id: number;
  nom: string;
  description?: string;
  canal: string;
  statut: string;
  budget?: number;
  dateDebut?: string;
  dateFin?: string;
  impressions: number;
  clics: number;
  conversions: number;
  tauxConversion: number;
  ctr: number;
  startupId: number;
  clientNom: string;
  expertId: number;
  prestataireNom: string;
  createdAt: string;
}

export interface CampagneRequest {
  nom: string;
  description?: string;
  canal: CanalCampagne;
  statut?: StatutCampagne;
  budget?: number;
  dateDebut?: string;
  dateFin?: string;
  impressions?: number;
  clics?: number;
  conversions?: number;
  startupId?: number;
}
