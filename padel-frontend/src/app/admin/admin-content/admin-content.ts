import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  OnDestroy,
  ViewChild,
  ElementRef,
  AfterViewInit
} from '@angular/core';
import {AsyncPipe, NgIf} from '@angular/common';
import { UiCard } from '../ui-card/ui-card';
import { DashboardService } from '../services/dashboard.service';
import { KpiGridWidget } from '../widgets/kpi-grid-widget/kpi-grid-widget';
import { Observable, Subject, Subscription, map, takeUntil } from 'rxjs';
import { DashboardData } from '../Interface/Dashboard-data';
import { Chart, ChartData, ChartOptions, registerables } from 'chart.js';
import { DashboardPeriod } from '../models/dashboard.model';

Chart.register(...registerables);

const ORDER = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

/**
 * ✔️ Typage fort (OBLIGATOIRE)
 */

@Component({
  selector: 'app-admin-content',
  imports: [AsyncPipe, UiCard, KpiGridWidget, NgIf],
  templateUrl: './admin-content.html',
  styleUrl: './admin-content.css',
  standalone: true,
})
export class AdminContent implements OnChanges, OnDestroy, AfterViewInit {

  @Input() selectedSiteId: number | 'ALL' = 'ALL';

  dashboardData$!: Observable<DashboardData>;

  @ViewChild('chartCanvas', { static: false })
  set chartCanvasRef(value: ElementRef<HTMLCanvasElement> | undefined) {
    this.chartCanvas = value;
    this.tryRenderChart();
  }

  private chartCanvas?: ElementRef<HTMLCanvasElement>;
  chart: Chart<'bar'> | null = null;
  private viewReady = false;
  private latestChartData: ChartData<'bar'> | null = null;
  private reservationsSub?: Subscription;

  private period: DashboardPeriod = '7d';

  /**
   * ✔️ Gestion mémoire (OBLIGATOIRE)
   */
  private destroy$ = new Subject<void>();

  constructor(private readonly dashboardService: DashboardService) {
    // Ne rien mettre ici pour le graphique (évite les problèmes de timing)
  }

  ngAfterViewInit(): void {
    this.viewReady = true;
    this.reload();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedSiteId']) {
      this.reload();
    }
  }

  ngOnDestroy(): void {
    this.reservationsSub?.unsubscribe();
    this.destroy$.next();
    this.destroy$.complete();
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  private reload(): void {
    this.dashboardData$ =
      this.dashboardService.getDashboardData(this.period, this.selectedSiteId);
    this.reservationsSub?.unsubscribe();
    this.reservationsSub = this.dashboardService.getReservationsPerDay(this.period, this.selectedSiteId).pipe(
      takeUntil(this.destroy$),
      map((points) => {
        console.log('[API] Réservations par jour (raw):', points); // DEBUG API
        const byDay: { [day: string]: number } = {};
        for (const pt of points) {
          const date = new Date(pt.label);
          if (Number.isNaN(date.getTime())) {
            continue;
          }
          const day = ORDER[date.getDay() === 0 ? 6 : date.getDay() - 1];
          byDay[day] = (byDay[day] ?? 0) + pt.value;
        }
        const chartData: ChartData<'bar'> = {
          labels: ORDER,
          datasets: [
            {
              label: 'Réservations par jour',
              data: ORDER.map((day) => byDay[day] ?? 0),
              backgroundColor: '#3b82f6',
            }
          ]
        };
        console.log('[DEBUG] Données formatées Chart.js:', chartData); // DEBUG CHART
        return chartData;
      })
    ).subscribe({
      next: (data) => {
        this.latestChartData = data;
        this.tryRenderChart();
      },
      error: (error) => {
        console.error('[API] Erreur réservations par jour:', error);
        // Fallback hardcodé pour valider le rendu du canvas même si l'API échoue.
        this.latestChartData = {
          labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'],
          datasets: [{
            label: 'Test hardcodé',
            data: [3, 7, 4, 9, 5, 2, 6],
            backgroundColor: '#22c55e'
          }]
        };
        this.tryRenderChart();
      }
    });
  }

  private tryRenderChart(): void {
    if (!this.viewReady || !this.chartCanvas?.nativeElement || !this.latestChartData) {
      return;
    }

    // Décale après le render courant pour éviter les races avec *ngIf.
    setTimeout(() => this.createChart(this.latestChartData as ChartData<'bar'>), 0);
  }

  private createChart(data: ChartData<'bar'>): void {
    if (!this.chartCanvas?.nativeElement) {
      return;
    }

    console.log('Canvas:', this.chartCanvas.nativeElement);
    if (this.chart) {
      this.chart.destroy();
    }

    const options: ChartOptions<'bar'> = {
      responsive: true,
      maintainAspectRatio: false,
      animation: false,
      plugins: {
        tooltip: { enabled: true },
        legend: { display: true }
      },
      scales: {
        x: {
          title: { display: true, text: 'Jour' },
          grid: { display: false }
        },
        y: {
          title: { display: true, text: 'Réservations' },
          beginAtZero: true,
          grid: { display: true }
        }
      }
    };

    this.chart = new Chart<'bar'>(this.chartCanvas.nativeElement, {
      type: 'bar',
      data,
      options,
    });
  }

  /**
   * ✔️ Correction logique + robustesse
   */
  // Suppression de la logique de traitement des données du graphique

  /**
   * ✔️ Encapsulation logique métier
   */
  // Suppression de la logique métier liée au graphique
}
