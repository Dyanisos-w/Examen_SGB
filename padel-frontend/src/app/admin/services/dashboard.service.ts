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

  readonly chartPeriods: { key: DashboardPeriod; label: string }[] = [
    { key: '7d',     label: '7 jours'       },
    { key: 'week',   label: 'Cette semaine'  },
    { key: 'next30d', label: 'Mois à venir' },
    { key: 'year',   label: 'Cette année'   },
  ];

  getDashboardData(period: DashboardPeriod = '7d', siteId: number | 'ALL' = 'ALL'): Observable<DashboardData> {
    return forkJoin({
      overview: this.http.get<DashboardOverviewApi>(`${this.apiUrl}/overview`, {
        params: { period: '7d', siteId: siteId === 'ALL' ? '' : siteId.toString() }
      }),
      reservations: this.http.get<DashboardReservationRowApi[]>(`${this.apiUrl}/reservations`, { params: { period } }),
      admins: this.http.get<DashboardMemberRowApi[]>(`${this.apiUrl}/admins`),
      members: this.http.get<DashboardMemberRowApi[]>(`${this.apiUrl}/members`),
      perDay: this.http.get<{ label: string; value: number }[]>(`${this.apiUrl}/reservations-per-day`, {
        params: { period, siteId: siteId === 'ALL' ? '' : siteId.toString() }
      }),
    }).pipe(
      map(({ overview, reservations, admins, members, perDay }) => {
        const filtered = siteId === 'ALL' ? reservations : reservations.filter(r => r.siteId === siteId);
        const filteredAdmins = siteId === 'ALL'
          ? admins
          : admins.filter(a => a.siteId === siteId || a.siteId === null);
        const filteredMembers = siteId === 'ALL'
          ? members
          : members.filter(m => m.siteId === siteId || m.siteId === null);
        return {
          kpis: this.mapKpis(overview),
          chartPoints: this.mapChartPoints(perDay, period),
          teamMembers: this.mapTeamMembers(filteredAdmins),
          taskItems: this.mapTaskItems(filtered),
          players: this.mapTeamMembers(filteredMembers),
        };
      })
    );
  }

  getReservationsPerDay(period: DashboardPeriod = '7d', siteId: number | 'ALL' = 'ALL') {
    return this.http.get<Record<string, number>>(
      `${this.apiUrl}/reservations-per-day`,
      { params: { period, siteId: siteId === 'ALL' ? '' : siteId.toString() } }
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

  private mapChartPoints(perDay: { label: string; value: number }[], period: DashboardPeriod): ChartPoint[] {
    if (period === 'next30d') {
      return this.groupByWeek(perDay);
    }
    if (period === 'year') {
      return this.groupByMonth(perDay);
    }
    const last7 = perDay.slice(-7);
    const maxValue = Math.max(...last7.map(p => p.value), 1);
    return last7.map(p => ({
      label: p.label.slice(5),
      value: Math.round((p.value / maxValue) * 100),
    }));
  }

  private groupByWeek(perDay: { label: string; value: number }[]): ChartPoint[] {
    const weekMap = new Map<string, number>();
    for (const p of perDay) {
      const d = new Date(p.label + 'T00:00:00');
      const weekNum = this.isoWeekNumber(d);
      const key = `S.${weekNum}`;
      weekMap.set(key, (weekMap.get(key) ?? 0) + p.value);
    }
    const entries = Array.from(weekMap.entries());
    const maxValue = Math.max(...entries.map(([, v]) => v), 1);
    return entries.map(([label, count]) => ({
      label,
      value: Math.round((count / maxValue) * 100),
    }));
  }

  private groupByMonth(perDay: { label: string; value: number }[]): ChartPoint[] {
    const monthNames = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jui', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'];
    const monthMap = new Map<string, number>();
    for (const p of perDay) {
      const d = new Date(p.label + 'T00:00:00');
      const key = monthNames[d.getMonth()];
      monthMap.set(key, (monthMap.get(key) ?? 0) + p.value);
    }
    const entries = Array.from(monthMap.entries());
    const maxValue = Math.max(...entries.map(([, v]) => v), 1);
    return entries.map(([label, count]) => ({
      label,
      value: Math.round((count / maxValue) * 100),
    }));
  }

  private isoWeekNumber(date: Date): number {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    d.setUTCDate(d.getUTCDate() + 4 - (d.getUTCDay() || 7));
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    return Math.ceil((((d.valueOf() - yearStart.valueOf()) / 86400000) + 1) / 7);
  }

  private mapTeamMembers(members: DashboardMemberRowApi[]): TeamMember[] {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return members
      .slice()
      .sort((a, b) => {
        const gA = a.siteNom ? `0_${a.siteNom}` : a.matricule.toUpperCase().startsWith('L') ? '1_Libre' : '2_Global';
        const gB = b.siteNom ? `0_${b.siteNom}` : b.matricule.toUpperCase().startsWith('L') ? '1_Libre' : '2_Global';
        const c = gA.localeCompare(gB);
        return c !== 0 ? c : `${a.nom} ${a.prenom}`.localeCompare(`${b.nom} ${b.prenom}`);
      })
      .map((member) => {
        const blockedUntil = member.interditReservationJusqua ? new Date(member.interditReservationJusqua) : null;
        const isBanned = blockedUntil !== null && blockedUntil >= today;
        return {
          matricule: member.matricule,
          name: `${member.prenom} ${member.nom}`,
          role: member.siteNom
            ? `Member - ${member.siteNom}`
            : member.matricule.toUpperCase().startsWith('L') ? 'Member - Libre' : 'Member - Global',
          status: isBanned ? 'busy' : 'online',
          isBanned,
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
    if (normalized.includes('ANNU')) return 'high';
    if (normalized.includes('ATTENTE') || normalized.includes('PENDING')) return 'medium';
    return 'low';
  }
}
