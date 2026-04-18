import { Component, EventEmitter, Output } from '@angular/core';
import { Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-admin-sidebar',
  imports: [
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './admin-sidebar.html',
  styleUrl: './admin-sidebar.css',
  standalone: true,
})
export class AdminSidebar {
  @Input() isOpen = false;
  @Output() navigate = new EventEmitter<void>();

  readonly isGlobalAdmin = this.readRoleFromToken() === 'ROLE_GLOBALADMIN';

  private readRoleFromToken(): string | null {
    const token = sessionStorage.getItem('access_token');
    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.role ?? null;
    } catch {
      return null;
    }
  }
}
