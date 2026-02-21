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
  statutReservation: string;
  participants: ParticipantPayment[];
}
