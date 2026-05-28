import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminClosureService, SiteClosureDto } from '../services/admin-opening-hours.service';
import { SiteDto, SiteService } from '../../services/site.service';
import { NotificationService } from '../../services/notification.service';
import { SharedSidebarMenu } from '../../layout/shared-sidebar-menu/shared-sidebar-menu';
import { AdminTopbar } from '../admin-topbar/admin-topbar';

@Component({
  selector: 'app-admin-closures',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SharedSidebarMenu, AdminTopbar],
  templateUrl: './admin-closures.html'
})
export class AdminClosuresComponent {
  private readonly fb = inject(FormBuilder);
  private readonly closureService = inject(AdminClosureService);
  private readonly siteService = inject(SiteService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly cdr = inject(ChangeDetectorRef);

  isSidebarOpen = false;
  isLoading = false;
  isGlobalAdmin = false;
  adminSiteId: number | null = null;
  sites: SiteDto[] = [];
  selectedSiteId: number | null = null; // null = global
  closures: SiteClosureDto[] = [];

  form = this.fb.group({
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    reason: ['']
  });

  constructor() {
    const auth = this.readAuthFromToken();
    this.isGlobalAdmin = auth.role === 'ROLE_GLOBALADMIN';
    this.adminSiteId = auth.siteId;

    if (!this.isGlobalAdmin) {
      this.selectedSiteId = this.adminSiteId;
    }

    this.siteService.getSites().subscribe({
      next: (sites) => {
        this.sites = this.isGlobalAdmin
          ? sites
          : sites.filter(s => s.siteId === this.adminSiteId);
        this.cdr.detectChanges();
      },
      error: () => { this.notification.error('Impossible de charger les sites.'); }
    });

    this.loadClosures();
  }

  toggleSidebar(): void { this.isSidebarOpen = !this.isSidebarOpen; }
  closeSidebar(): void { this.isSidebarOpen = false; }
  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }

  onSiteChange(value: string): void {
    this.selectedSiteId = value === '' ? null : Number(value);
    this.loadClosures();
  }

  loadClosures(): void {
    const obs = this.selectedSiteId === null
      ? this.closureService.getGlobalClosures()
      : this.closureService.getSiteClosures(this.selectedSiteId);

    obs.subscribe({
      next: (closures) => { this.closures = closures; this.cdr.detectChanges(); },
      error: () => { this.notification.error('Impossible de charger les fermetures.'); }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { startDate, endDate, reason } = this.form.getRawValue();
    const body = {
      siteId: this.selectedSiteId,
      applyToAll: this.selectedSiteId === null,
      startDate: startDate!,
      endDate: endDate!,
      reason: reason?.trim() || null
    };

    this.isLoading = true;
    this.closureService.createClosure(body).subscribe({
      next: () => {
        this.notification.success('Fermeture ajoutée avec succès.');
        this.form.reset();
        this.loadClosures();
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 400) {
          this.notification.error(error.error?.message || 'Données invalides.');
          return;
        }
        if (error.status === 409) {
          this.notification.error(error.error?.message || 'Une fermeture identique existe déjà.');
          return;
        }
        if (error.status === 403) {
          this.notification.error('Accès refusé.');
          return;
        }
        this.notification.error('Erreur serveur. Réessayez plus tard.');
      }
    });
  }

  deleteClosure(id: number): void {
    this.closureService.deleteClosure(id).subscribe({
      next: () => {
        this.notification.success('Fermeture supprimée.');
        this.loadClosures();
      },
      error: () => { this.notification.error('Impossible de supprimer cette fermeture.'); }
    });
  }

  formatDate(dateStr: string): string {
    const [year, month, day] = dateStr.split('-');
    return `${day}/${month}/${year}`;
  }

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
