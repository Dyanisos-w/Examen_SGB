import { KpiItem } from './Kpi-item';
import { ChartPoint } from './Chart-point';
import { TeamMember } from './Team-member';
import { TaskItem } from './Task-item';

export interface DashboardData {
  kpis: KpiItem[];
  chartPoints: ChartPoint[];
  teamMembers: TeamMember[];
  taskItems: TaskItem[];
  players: TeamMember[];
}
