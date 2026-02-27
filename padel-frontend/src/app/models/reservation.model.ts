import { Participant } from './participant.model';

export interface Reservation {
  id: number;
  dateHeure: string;
  siteNom: string;
  terrainNom: string;
  statutReservation: string;
  participants: Participant[];
  organisateur: Participant;
}

