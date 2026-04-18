import { Injectable } from '@angular/core';
import {Observable, of} from 'rxjs';
import {KpiItem} from '../../admin/Interface/Kpi-item';
import {ChartPoint} from '../../admin/Interface/Chart-point';
import {TeamMember} from '../../admin/Interface/Team-member';
import {TaskItem} from '../../admin/Interface/Task-item';
 import {DashboardData} from '../../admin/Interface/Dashboard-data';


@Injectable({
  providedIn: 'root',
})
export class DashboardService {
  constructor() {
  }

getDashboardData(): Observable<DashboardData> {
    const kpis : KpiItem [] = [
      { label: 'Reservations', value: 128, delta: 12, trend: 'up' },
      { label: 'Revenue (€)', value: 1920, delta: -5, trend: 'down' },
      { label: 'Players', value: 342, delta: 8, trend: 'up' },
      { label: 'Occupancy (%)', value: 76, delta: 3, trend: 'up' }
    ];

  const chart: ChartPoint[] = [
    { label: 'Mon', value: 20 },
    { label: 'Tue', value: 35 },
    { label: 'Wed', value: 28 },
    { label: 'Thu', value: 40 },
    { label: 'Fri', value: 55 },
    { label: 'Sat', value: 70 },
    { label: 'Sun', value: 60 }
  ];

  const team: TeamMember[] = [
    { name: 'Alice Dupont', role: 'Admin Local', status: 'online' },
    { name: 'Bob Martin', role: 'Manager', status: 'offline' },
    { name: 'Charlie Durand', role: 'Coach', status: 'busy' }
  ];

  const tasks: TaskItem[] = [
    { title: 'Validate payments', priority: 'high', dueLabel: 'Today', done: false },
    { title: 'Update planning', priority: 'medium', dueLabel: 'Tomorrow', done: false },
    { title: 'Clean old reservations', priority: 'low', dueLabel: 'Next week', done: true }
  ];

  const mock: DashboardData = {
    kpis,
    chartPoints: chart,
    teamMembers: team,
    taskItems: tasks
  };

    return  of(mock);
}
}
