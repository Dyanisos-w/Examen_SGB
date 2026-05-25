import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminTerrainManagementService } from '../services/admin-terrain-management.service';
import { SiteDto, SiteService } from '../../services/site.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-admin-create-terrain',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-create-terrain.html',
  styleUrl: './admin-create-terrain.css'
})
export class AdminCreateTerrainComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminTerrainManagementService = inject(AdminTerrainManagementService);
  private readonly siteService = inject(SiteService);
  private readonly notification = inject(NotificationService);

  isLoading = false;
  sites: SiteDto[] = [];
  isGlobalAdmin = false;
  adminSiteId: number | null = null;

  form = this.fb.group({
    nom: ['', Validators.required],
    siteId: ['']
  });

  constructor() {
    const auth = this.readAuthFromToken();
    this.isGlobalAdmin = auth.role === 'ROLE_GLOBALADMIN';
    this.adminSiteId = auth.siteId;

    if (this.isGlobalAdmin) {
      this.form.get('siteId')?.setValidators(Validators.required);
      this.form.get('siteId')?.updateValueAndValidity();
    }

    this.loadSites();
  }

  private loadSites(): void {
    this.siteService.getSites().subscribe({
      next: (sites) => {
        this.sites = this.isGlobalAdmin
          ? sites
          : sites.filter((site) => site.siteId === this.adminSiteId);

        if (!this.isGlobalAdmin && this.sites.length === 1) {
          this.form.patchValue({ siteId: String(this.sites[0].siteId) });
        }
      },
      error: () => {
        this.notification.error('Impossible de charger les sites.');
      }
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nom, siteId } = this.form.getRawValue();
    const effectiveSiteId = this.isGlobalAdmin ? Number(siteId) : this.adminSiteId ?? undefined;

    this.isLoading = true;
    this.adminTerrainManagementService.createTerrain({
      nom: nom!.trim(),
      siteId: effectiveSiteId
    }).subscribe({
      next: (created) => {
        this.notification.success(`Terrain créé : ${created.nom} (id: ${created.terrainId})`);
        this.form.reset({ nom: '', siteId: this.isGlobalAdmin ? '' : String(this.adminSiteId ?? '') });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 403) {
          this.notification.error('Accès refusé pour ce site.');
          return;
        }
        if (error.status === 400) {
          this.notification.error(error.error?.message || 'Données invalides. Vérifiez les champs saisis.');
          return;
        }
        this.notification.error('Erreur serveur. Réessayez plus tard.');
      }
    });
  }

  private readAuthFromToken(): { role: string | null; siteId: number | null } {
    const token = sessionStorage.getItem('access_token');
    if (!token) return { role: null, siteId: null };

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        role: payload?.role ?? null,
        siteId: payload?.siteId ?? null
      };
    } catch {
      return { role: null, siteId: null };
    }
  }
}
