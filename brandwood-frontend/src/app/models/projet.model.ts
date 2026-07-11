// Modèles du Module 3 — Gestion des Projets IT.

export type StatutProjet = 'PLANIFIE' | 'EN_COURS' | 'EN_PAUSE' | 'TERMINE' | 'ANNULE';
export type StatutJalon = 'A_VENIR' | 'ATTEINT' | 'EN_RETARD';

export interface Projet {
  id: number;
  nom: string;
  description?: string;
  dateDebut?: string;
  dateFin?: string;
  budget?: number;
  statut: StatutProjet;
  progression: number;
  technologies: string[];
  startupId: number;
  clientNom: string;
  expertId: number;
  prestataireNom: string;
  nbJalons: number;
  jalonsAtteints: number;
  nbMembres: number;
  createdAt: string;
}

export interface ProjetRequest {
  nom: string;
  description?: string;
  dateDebut?: string;
  dateFin?: string;
  budget?: number;
  statut?: StatutProjet;
  technologies?: string[];
  startupId?: number;
}

export interface Jalon {
  id: number;
  nom: string;
  datePrevue?: string;
  statut: string;        // peut valoir EN_RETARD (dérivé) en plus de A_VENIR/ATTEINT
  description?: string;
  projetId: number;
}

export interface JalonRequest {
  nom: string;
  datePrevue?: string;
  description?: string;
  statut?: StatutJalon;
}

// ── Module 4 : Équipe & Membres ──
export type RoleProjet = 'CHEF_PROJET' | 'DEVELOPPEUR' | 'DESIGNER' | 'TESTEUR' | 'ANALYSTE';

export interface Membre {
  id: number;
  prenom: string;
  nom: string;
  email?: string;
  roleProjet: string;
  projetId: number;
}

export interface MembreRequest {
  prenom: string;
  nom: string;
  email?: string;
  roleProjet: RoleProjet;
}

// ── Module 5 : Tâches & Kanban ──
export type StatutTache = 'A_FAIRE' | 'EN_COURS' | 'EN_REVISION' | 'TERMINE';
export type PrioriteTache = 'BASSE' | 'MOYENNE' | 'HAUTE' | 'CRITIQUE';

export interface Tache {
  id: number;
  titre: string;
  description?: string;
  statut: string;
  priorite: string;
  dateEcheance?: string;
  enRetard: boolean;
  position: number;
  projetId: number;
  membreId?: number;
  membreNom?: string;
}

export interface TacheRequest {
  titre: string;
  description?: string;
  priorite?: PrioriteTache;
  dateEcheance?: string;
  statut?: StatutTache;
  membreId?: number;
}

export interface AnalyseRisque {
  niveau: string;
  score: number;
  message: string;
  facteurs: string[];
  source: string;
}

export interface GenerationTaches {
  taches: Tache[];
  nbCreees: number;
  source: string;
  message: string;
}

// ── Module 6 : Calendrier & Réunions ──
export type StatutReunion = 'PLANIFIEE' | 'TERMINEE' | 'ANNULEE';

export interface Reunion {
  id: number;
  titre: string;
  description?: string;
  dateHeure: string;
  dureeMinutes?: number;
  lien?: string;
  statut: string;
  projetId: number;
  projetNom: string;
}

export interface ReunionRequest {
  titre: string;
  description?: string;
  dateHeure: string;
  dureeMinutes?: number;
  lien?: string;
  statut?: StatutReunion;
}

// ── Module 8 : Rapport projet ──
export interface RapportProjet {
  generatedAt: string;
  projetId: number;
  nom: string;
  description?: string;
  statut: string;
  progression: number;
  budget?: number;
  dateDebut?: string;
  dateFin?: string;
  technologies: string[];
  clientNom: string;
  prestataireNom: string;
  jalonsTotal: number;
  jalonsAtteints: number;
  tachesTotal: number;
  tachesTerminees: number;
  tachesEnCours: number;
  tachesEnRetard: number;
  nbMembres: number;
  nbReunions: number;
  membres: Membre[];
}
