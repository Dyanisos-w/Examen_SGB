import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { KpiItem } from '../Interface/Kpi-item';
import { ChartPoint } from '../Interface/Chart-point';
import { TeamMember } from '../Interface/Team-member';
import { TaskItem } from '../Interface/Task-item';
import { DashboardData } from '../Interface/Dashboard-data';
import {
  DashboardMemberRowApi,
  DashboardOverviewApi,
  DashboardPeriod,
  DashboardReservationRowApi,
} from '../models/dashboard.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  private readonly apiUrl = `${environment.apiBaseUrl}/api/admin/dashboard`;

  constructor(private readonly http: HttpClient) {}

  getDashboardData(period: DashboardPeriod = '7d'): Observable<DashboardData> {
	return forkJoin({
	  overview: this.http.get<DashboardOverviewApi>(`${this.apiUrl}/overview`, {
		params: { period },
	  }),
	  reservations: this.http.get<DashboardReservationRowApi[]>(`${this.apiUrl}/reservations`, {
		params: { period },
	  }),
	  members: this.http.get<DashboardMemberRowApi[]>(`${this.apiUrl}/members`),
	}).pipe(
	  map(({ overview, reservations, members }) => ({
		kpis: this.mapKpis(overview),
		chartPoints: this.mapChartPoints(reservations),
		teamMembers: this.mapTeamMembers(members),
		taskItems: this.mapTaskItems(reservations),
	  }))
	);
  }

  private mapKpis(overview: DashboardOverviewApi): KpiItem[] {
	return [
	  { label: 'Reservations', value: Math.round(overview.totalReservations), delta: 0, trend: 'flat' },
	  { label: 'Revenue (EUR)', value: Math.round(overview.totalRevenue), delta: 0, trend: 'flat' },
	  { label: 'Players', value: Math.round(overview.totalUsers), delta: 0, trend: 'flat' },
	  { label: 'Occupancy (%)', value: Math.round(overview.occupancyRate), delta: 0, trend: 'flat' },
	];
  }

  private mapChartPoints(reservations: DashboardReservationRowApi[]): ChartPoint[] {
	const byDate = new Map<string, number>();

	for (const reservation of reservations) {
	  const date = reservation.dateReservation;
	  byDate.set(date, (byDate.get(date) ?? 0) + 1);
	}

	return Array.from(byDate.entries())
	  .sort(([a], [b]) => a.localeCompare(b))
	  .map(([date, count]) => ({
		label: date.slice(5),
		value: count,
	  }));
  }

  private mapTeamMembers(members: DashboardMemberRowApi[]): TeamMember[] {
	const today = new Date();
	today.setHours(0, 0, 0, 0);

	return members.slice(0, 6).map((member) => {
	  const blockedUntil = member.interditReservationJusqua ? new Date(member.interditReservationJusqua) : null;
	  const isBlocked = blockedUntil !== null && blockedUntil >= today;

	  return {
		name: `${member.prenom} ${member.nom}`,
		role: member.siteNom ? `Member - ${member.siteNom}` : 'Member - Global',
		status: isBlocked ? 'busy' : 'online',
	  };
	});
  }

  private mapTaskItems(reservations: DashboardReservationRowApi[]): TaskItem[] {
	return reservations.slice(0, 5).map((reservation) => ({
	  title: `Reservation #${reservation.reservationId} - ${reservation.statut}`,
	  priority: this.priorityFromStatus(reservation.statut),
	  dueLabel: reservation.dateReservation,
	  done: ['CONFIRMED', 'PAYE', 'PAID'].includes((reservation.statut ?? '').toUpperCase()),
	}));
  }

  private priorityFromStatus(status: string): TaskItem['priority'] {
	const normalized = (status ?? '').toUpperCase();
	if (normalized.includes('ANNU')) {
	  return 'high';
	}
	if (normalized.includes('ATTENTE') || normalized.includes('PENDING')) {
	  return 'medium';
	}
	return 'low';
  }
}


