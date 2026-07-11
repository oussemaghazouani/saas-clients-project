// Modèles du Module 9 — Support & Tickets.

export type StatutTicket = 'OUVERT' | 'EN_COURS' | 'RESOLU' | 'FERME';
export type PrioriteTicket = 'BASSE' | 'MOYENNE' | 'HAUTE' | 'URGENTE';

export interface Message {
  id: number;
  auteurNom: string;
  auteurRole: string;
  contenu: string;
  dateEnvoi: string;
}

export interface Ticket {
  id: number;
  sujet: string;
  description?: string;
  statut: string;
  priorite: string;
  clientNom: string;
  prestataireNom: string;
  nbMessages: number;
  createdAt: string;
  messages?: Message[];
}

export interface TicketRequest {
  sujet: string;
  description?: string;
  priorite?: PrioriteTicket;
}
