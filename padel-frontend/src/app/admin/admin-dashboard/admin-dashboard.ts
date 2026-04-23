import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AdminTopbar } from '../admin-topbar/admin-topbar';
import { AdminContent } from '../admin-content/admin-content';
import { SharedSidebarMenu } from '../../layout/shared-sidebar-menu/shared-sidebar-menu';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, SharedSidebarMenu, AdminTopbar, AdminContent],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css'
})
export class AdminDashboard {
  isSidebarOpen = false;
  selectedSiteId: number | 'ALL' = 'ALL';

  constructor(private router: Router) {}

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
  }

  onSiteChange(id: number | 'ALL'): void {
    this.selectedSiteId = id;
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }
}
