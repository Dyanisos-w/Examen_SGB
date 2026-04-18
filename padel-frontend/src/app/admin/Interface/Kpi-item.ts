export interface KpiItem {
  label: string;
  value: number;
  delta: number;
  trend: 'up' | 'down' | 'flat';
}
