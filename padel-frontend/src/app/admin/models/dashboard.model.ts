export type DashboardPeriod = 'today' | '7d' | 'month' | 'currentmonth' | 'year';

export interface DashboardOverviewApi {
  totalReservations: number;
  totalRevenue: number;
  totalUsers: number;
  occupancyRate: number;
  cancellationRate: number;
}

export interface DashboardReservationRowApi {
  reservationId: number;
  dateReservation: string;
  heureDebut: string;
  heureFin: string;
  statut: string;
  typeReservation: string;
  montantTotal: number | null;
  utilisateurMatricule: string;
  utilisateurNom: string;
  terrainId: number | null;
  terrainNom: string | null;
  siteId: number | null;
  siteNom: string | null;
}

export interface DashboardMemberRowApi {
  matricule: string;
  nom: string;
  prenom: string;
  siteId: number | null;
  siteNom: string | null;
  interditReservationJusqua: string | null;
}


