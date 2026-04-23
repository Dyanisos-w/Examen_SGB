import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {AsyncPipe} from '@angular/common';
import {UiCard} from '../ui-card/ui-card';
import {DashboardService} from '../services/dashboard.service';
import {KpiGridWidget} from '../widgets/kpi-grid-widget/kpi-grid-widget';
import {Observable} from 'rxjs';
import {DashboardData} from '../Interface/Dashboard-data';

@Component({
  selector: 'app-admin-content',
  imports: [AsyncPipe, UiCard, KpiGridWidget],
  templateUrl: './admin-content.html',
  styleUrl: './admin-content.css',
  standalone: true,
})
export class AdminContent implements OnChanges {
  @Input() selectedSiteId: number | 'ALL' = 'ALL';
  dashboardData$!: Observable<DashboardData>;

  constructor(private readonly dashboardService: DashboardService) {
    this.reload();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedSiteId']) {
      this.reload();
    }
  }

  private reload(): void {
    this.dashboardData$ = this.dashboardService.getDashboardData('7d', this.selectedSiteId);
  }
}
