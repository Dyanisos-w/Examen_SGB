import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { AsyncPipe, NgClass } from '@angular/common';
import { UiCard } from '../ui-card/ui-card';
import { DashboardService } from '../services/dashboard.service';
import { AdminUserManagementService } from '../services/admin-user-management.service';
import { KpiGridWidget } from '../widgets/kpi-grid-widget/kpi-grid-widget';
import { Observable } from 'rxjs';
import { DashboardData } from '../Interface/Dashboard-data';

@Component({
  selector: 'app-admin-content',
  imports: [AsyncPipe, NgClass, UiCard, KpiGridWidget],
  templateUrl: './admin-content.html',
  styleUrl: './admin-content.css',
  standalone: true,
})
export class AdminContent implements OnChanges {
  @Input() selectedSiteId: number | 'ALL' = 'ALL';

  dashboardData$!: Observable<DashboardData>;

  activeTab: 'admins' | 'players' = 'admins';
  selectedMember: string | null = null;
  isTabDropdownOpen = false;

  confirmModal: { visible: boolean; action: () => void; message: string } = {
    visible: false,
    action: () => {},
    message: '',
  };

  constructor(
    private readonly dashboardService: DashboardService,
    private readonly adminUserMgmtService: AdminUserManagementService,
  ) {
    this.reload();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['selectedSiteId']) {
      this.reload();
    }
  }

  isGlobalAdmin(): boolean {
    const token = sessionStorage.getItem('access_token');
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role === 'ROLE_GLOBALADMIN';
    } catch {
      return false;
    }
  }

  switchTab(tab: 'admins' | 'players'): void {
    this.activeTab = tab;
    this.selectedMember = null;
    this.isTabDropdownOpen = false;
  }

  toggleMember(matricule: string): void {
    this.selectedMember = this.selectedMember === matricule ? null : matricule;
  }

  openConfirm(message: string, action: () => void): void {
    this.confirmModal = { visible: true, action, message };
  }

  closeConfirm(): void {
    this.confirmModal = { ...this.confirmModal, visible: false };
  }

  confirmAction(): void {
    this.confirmModal.action();
    this.closeConfirm();
  }

  revokeAdmin(matricule: string): void {
    this.openConfirm(
      'Supprimer définitivement cet administrateur local ?',
      () => this.adminUserMgmtService.revokeLocalAdmin(matricule).subscribe({
        next: () => this.reload(),
      })
    );
  }

  banPlayer(matricule: string): void {
    this.openConfirm(
      'Bannir ce joueur indéfiniment ?',
      () => this.adminUserMgmtService.banPlayer(matricule).subscribe({
        next: () => this.reload(),
      })
    );
  }

  unbanPlayer(matricule: string): void {
    this.openConfirm(
      'Lever le ban de ce joueur ?',
      () => this.adminUserMgmtService.unbanPlayer(matricule).subscribe({
        next: () => this.reload(),
      })
    );
  }

  private reload(): void {
    this.selectedMember = null;
    this.dashboardData$ = this.dashboardService.getDashboardData('7d', this.selectedSiteId);
  }
}
