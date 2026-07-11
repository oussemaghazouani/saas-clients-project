// Modèles partagés du Module 2 — Gestion des Utilisateurs, Accès & Packs.

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export type StatutCompte = 'ACTIF' | 'DESACTIVE' | 'SUSPENDU' | 'SUPPRIME';
export type StatutAbonnement = 'EN_ATTENTE' | 'ACTIF' | 'EXPIRE' | 'ANNULE';
export type MethodePaiement = 'VIREMENT' | 'CARTE_BANCAIRE';
export type ActionStatut = 'ACTIVER' | 'DESACTIVER' | 'SUSPENDRE';

export interface Pack {
  id: number;
  nom: string;
  description?: string;
  prix: number;
  nbProjetsMax: number;
  nbClientsMax: number;
  dureeMois: number;
  actif: boolean;
}

export interface PackRequest {
  nom: string;
  description?: string;
  prix: number;
  nbProjetsMax: number;
  nbClientsMax: number;
  dureeMois: number;
  actif?: boolean;
}

export interface Prestataire {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  specialite?: string;
  tarifHoraire?: number;
  disponibilite: boolean;
  competences: string[];
  packId?: number;
  packNom?: string;
  statutCompte: StatutCompte;
  nbClients: number;
  createdAt: string;
}

export interface Client {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  identifiantUnique: string;
  domaineActivite?: string;
  siret?: string;
  adresse?: string;
  nombreEmployes?: number;
  actif: boolean;
  createdAt: string;
}

export interface ProvisionClientRequest {
  firstName: string;
  lastName: string;
  phone?: string;
  password: string;
  domaineActivite?: string;
  siret?: string;
  adresse?: string;
  nombreEmployes?: number;
}

export interface ProvisionClientResponse {
  startupId: number;
  userId: number;
  identifiantUnique: string;
  message: string;
}

export interface Abonnement {
  id: number;
  expertId: number;
  prestataireNom: string;
  packId: number;
  packNom: string;
  dateDebut?: string;
  dateFin?: string;
  statut: StatutAbonnement;
  methodePaiement: MethodePaiement;
  essaiGratuit: boolean;
}

export interface SouscriptionRequest {
  packId: number;
  methodePaiement: MethodePaiement;
}

export interface Profil {
  userId: number;
  userType: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  // Prestataire
  specialite?: string;
  tarifHoraire?: number;
  disponibilite?: boolean;
  competences?: string[];
  packId?: number;
  packNom?: string;
  statutCompte?: string;
  // Client
  domaineActivite?: string;
  siret?: string;
  adresse?: string;
  nombreEmployes?: number;
  identifiantUnique?: string;
}

export interface UpdateProfilRequest {
  firstName?: string;
  lastName?: string;
  phone?: string;
  specialite?: string;
  tarifHoraire?: number;
  disponibilite?: boolean;
  competences?: string[];
  domaineActivite?: string;
  siret?: string;
  adresse?: string;
  nombreEmployes?: number;
}
