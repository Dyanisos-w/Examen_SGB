import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AdminOpeningHoursService, OpeningHoursDayDto } from '../services/admin-opening-hours.service';
import { SiteDto, SiteService } from '../../services/site.service';
import { NotificationService } from '../../services/notification.service';
import { SharedSidebarMenu } from '../../layout/shared-sidebar-menu/shared-sidebar-menu';
import { AdminTopbar } from '../admin-topbar/admin-topbar';

/** Libellés français des jours de la semaine. */
const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Lundi', TUESDAY: 'Mardi', WEDNESDAY: 'Mercredi',
  THURSDAY: 'Jeudi', FRIDAY: 'Vendredi', SATURDAY: 'Samedi', SUNDAY: 'Dimanche'
};

/** Ordre ISO lundi → dimanche. */
const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

@Component({
  selector: 'app-admin-opening-hours',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SharedSidebarMenu, AdminTopbar],
  templateUrl: './admin-opening-hours.html',
  styleUrl: './admin-opening-hours.css'
})
export class AdminOpeningHoursComponent {
  private readonly fb = inject(FormBuilder);
  private readonly openingHoursService = inject(AdminOpeningHoursService);
  private readonly siteService = inject(SiteService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  isSidebarOpen = false;
  isLoading = false;
  isGlobalAdmin = false;
  adminSiteId: number | null = null;
  sites: SiteDto[] = [];

  /** 'GLOBAL' ou l'id du site sous forme de string — pilote le <select>. */
  selectedSiteKey = '';

  /** Mode courant : global = applique à tous les sites, site = un seul. */
  mode: 'global' | 'site' | null = null;

  /** Id du site sélectionné (null en mode global). */
  selectedSiteId: number | null = null;

  readonly dayLabels = DAY_LABELS;
  readonly dayOrder = DAY_ORDER;

  /** Formulaire réactif : un FormGroup par jour dans un FormArray. */
  hoursForm = this.fb.group({ days: this.fb.array([]) });

  get hoursDays(): FormArray { return this.hoursForm.get('days') as FormArray; }

  constructor() {
    const auth = this.readAuthFromToken();
    this.isGlobalAdmin = auth.role === 'ROLE_GLOBALADMIN';
    this.adminSiteId = auth.siteId;

    this.initHoursForm();
    this.loadSites();

    if (this.isGlobalAdmin) {
      // GlobalAdmin démarre en mode Global par défaut
      this.selectedSiteKey = 'GLOBAL';
      this.mode = 'global';
    } else if (this.adminSiteId) {
      // LocalAdmin : site pré-sélectionné, pas de choix
      this.selectedSiteKey = String(this.adminSiteId);
      this.mode = 'site';
      this.selectedSiteId = this.adminSiteId;
      this.loadHours(this.adminSiteId);
    }
  }

  toggleSidebar(): void { this.isSidebarOpen = !this.isSidebarOpen; }
  closeSidebar(): void { this.isSidebarOpen = false; }
  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }

  /** Réinitialise le FormArray avec les valeurs par défaut (8h-22h, ouvert). */
  private initHoursForm(): void {
    const fa = this.hoursForm.get('days') as FormArray;
    fa.clear();
    for (const day of DAY_ORDER) {
      fa.push(this.fb.group({
        dayOfWeek: [day],
        openingTime: ['08:00'],
        closingTime: ['22:00'],
        closed: [false]
      }));
    }
  }

  private loadSites(): void {
    this.siteService.getSites().subscribe({
      next: (sites) => {
        this.sites = this.isGlobalAdmin
          ? sites
          : sites.filter(s => s.siteId === this.adminSiteId);
        this.cdr.detectChanges();
      },
      error: () => { this.notification.error('Impossible de charger les sites.'); }
    });
  }

  /** Appelé quand le GlobalAdmin change de valeur dans le dropdown. */
  onSiteChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedSiteKey = value;

    if (value === 'GLOBAL') {
      this.mode = 'global';
      this.selectedSiteId = null;
      // Mode global : on repart sur les valeurs par défaut (pas de config globale en base)
      this.initHoursForm();
    } else if (value) {
      this.mode = 'site';
      this.selectedSiteId = Number(value);
      this.loadHours(this.selectedSiteId);
    } else {
      this.mode = null;
      this.selectedSiteId = null;
    }
  }

  /** Charge les horaires d'un site depuis l'API et met à jour le FormArray. */
  private loadHours(siteId: number): void {
    this.openingHoursService.getOpeningHours(siteId).subscribe({
      next: (res) => {
        const fa = this.hoursForm.get('days') as FormArray;
        DAY_ORDER.forEach((key, i) => {
          const dto = res.days.find(d => d.dayOfWeek === key);
          fa.at(i).patchValue({
            dayOfWeek: key,
            openingTime: dto?.openingTime ? dto.openingTime.substring(0, 5) : '08:00',
            closingTime: dto?.closingTime ? dto.closingTime.substring(0, 5) : '22:00',
            closed: dto?.closed ?? false
          });
        });
        this.cdr.detectChanges();
      },
      error: () => { this.notification.error('Impossible de charger les horaires.'); }
    });
  }

  /**
   * Bascule le flag "fermé" d'un jour.
   * Fermé → vide les heures ; ouvert → remet les valeurs par défaut.
   */
  toggleClosed(index: number): void {
    const ctrl = this.hoursDays.at(index);
    if (ctrl.get('closed')?.value) {
      ctrl.patchValue({ openingTime: '', closingTime: '' });
    } else {
      ctrl.patchValue({ openingTime: '08:00', closingTime: '22:00' });
    }
  }

  /** Construit le payload à partir du FormArray. */
  private buildPayload(): { days: OpeningHoursDayDto[] } {
    return {
      days: this.hoursDays.controls.map(ctrl => {
        const v = ctrl.getRawValue();
        return {
          dayOfWeek: v.dayOfWeek,
          // Formater en HH:mm:ss attendu par le backend
          openingTime: v.closed ? null : (v.openingTime ? `${v.openingTime}:00` : null),
          closingTime: v.closed ? null : (v.closingTime ? `${v.closingTime}:00` : null),
          closed: v.closed
        };
      })
    };
  }

  /** Sauvegarde les horaires — applique à tous les sites (global) ou un seul (site). */
  onSubmitHours(): void {
    const body = this.buildPayload();
    this.isLoading = true;

    if (this.mode === 'global') {
      // Applique en parallèle à chaque site connu
      forkJoin(
        this.sites.map(s => this.openingHoursService.updateOpeningHours(s.siteId, body))
      ).subscribe({
        next: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
          this.notification.success('Horaires sauvegardés pour tous les sites.');
        },
        error: (err) => {
          this.isLoading = false;
          this.cdr.detectChanges();
          this.notification.error(err.error?.message || 'Erreur lors de la sauvegarde.');
        }
      });
    } else if (this.selectedSiteId != null) {
      this.openingHoursService.updateOpeningHours(this.selectedSiteId, body).subscribe({
        next: () => {
          this.isLoading = false;
          this.cdr.detectChanges();
          this.notification.success('Horaires sauvegardés.');
        },
        error: (err) => {
          this.isLoading = false;
          this.cdr.detectChanges();
          this.notification.error(err.error?.message || 'Erreur lors de la sauvegarde.');
        }
      });
    }
  }

  /** Extrait le rôle et le siteId depuis le JWT stocké en session. */
  private readAuthFromToken(): { role: string | null; siteId: number | null } {
    const token = sessionStorage.getItem('access_token');
    if (!token) return { role: null, siteId: null };
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return { role: payload?.role ?? null, siteId: payload?.siteId ?? null };
    } catch {
      return { role: null, siteId: null };
    }
  }
}
