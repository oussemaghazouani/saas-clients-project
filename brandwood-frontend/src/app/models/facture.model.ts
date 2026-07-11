// Modèles du Module 10 — Facturation.

export type StatutFacture = 'BROUILLON' | 'ENVOYEE' | 'PAYEE' | 'ANNULEE';

export interface LigneFacture {
  id?: number;
  description: string;
  quantite: number;
  prixUnitaire: number;
  montant?: number;
}

export interface Facture {
  id: number;
  numero: string;
  startupId: number;
  clientNom: string;
  expertId: number;
  prestataireNom: string;
  projetId?: number;
  projetNom?: string;
  montantHt: number;
  tauxTva: number;
  montantTtc: number;
  statut: string;
  dateEmission?: string;
  dateEcheance?: string;
  lignes: LigneFacture[];
  createdAt: string;
}

export interface LigneRequest {
  description: string;
  quantite: number;
  prixUnitaire: number;
}

export interface FactureRequest {
  startupId: number;
  projetId?: number;
  tauxTva?: number;
  dateEcheance?: string;
  lignes: LigneRequest[];
}
