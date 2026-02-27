export interface Payment {
  participantMatricule: string;
  reservationId: number;
  status: 'A_PAYER' | 'PAYE';
  amount?: number;
}

