import { Component } from '@angular/core';
import { AsyncPipe } from '@angular/common';
import { UiCard } from '../ui-card/ui-card';
import { DashboardService } from '../services/dashboard.service';
import { KpiGridWidget } from '../widgets/kpi-grid-widget/kpi-grid-widget';

@Component({
  selector: 'app-admin-content',
  imports: [AsyncPipe, UiCard, KpiGridWidget],
  templateUrl: './admin-content.html',
  styleUrl: './admin-content.css',
  standalone: true,
})
export class AdminContent {
  readonly dashboardData$;

  constructor(private readonly dashboardService: DashboardService) {
    this.dashboardData$ = this.dashboardService.getDashboardData();
  }
}
