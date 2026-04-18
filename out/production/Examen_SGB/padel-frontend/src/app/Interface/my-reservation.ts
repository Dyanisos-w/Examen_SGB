export interface ParticipantPayment {
  matricule: string;
  nom: string;
  prenom: string;
  paymentStatus: 'A_PAYER' | 'PAYE';
  isMe: boolean;
}

export interface MyReservation {
  reservationId: number;
  dateHeure: string;
  siteNom: string;
  terrainNom: string;
  typeReservation: 'PUBLIC' | 'PRIVATE';
  statutReservation: string;
  participants: ParticipantPayment[];
  isOrganizer: boolean;
}

export interface PublicReservation {
  reservationId: number;
  dateHeure: string;
  siteNom: string;
  terrainNom: string;
  nbJoueurs: number;
  statut: string;
}
