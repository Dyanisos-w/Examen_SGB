import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AdminTopbar } from '../admin-topbar/admin-topbar';
import { AdminContent } from '../admin-content/admin-content';
import { SharedSidebarMenu } from '../../layout/shared-sidebar-menu/shared-sidebar-menu';
import { AdminSiteContextService } from '../services/admin-site-context.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, SharedSidebarMenu, AdminTopbar, AdminContent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard {
  isSidebarOpen = false;

  private readonly siteContext = inject(AdminSiteContextService);
  private readonly router = inject(Router);

  // Lire le site actif depuis le service partagé
  get selectedSiteId(): number | 'ALL' {
    const id = this.siteContext.selectedSiteId;
    return id === null ? 'ALL' : id;
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  /** Le menu émet encore cet événement pour rétrocompatibilité, le service est déjà mis à jour. */
  onSiteChange(_id: number | 'ALL'): void {
    // Pas besoin de faire quoi que ce soit ici,
    // AdminSiteContextService est déjà la source unique.
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }
}
