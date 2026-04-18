import { Component, EventEmitter, Output } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-topbar',
  imports: [],
  templateUrl: './admin-topbar.html',
  styleUrl: './admin-topbar.css',
  standalone: true,
})
export class AdminTopbar {
  @Output() menuClick = new EventEmitter<void>();

  constructor(private router: Router) {}

  get username(): string {
    const token = sessionStorage.getItem('access_token');
    if (!token) return '';
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.sub ?? '';
    } catch { return ''; }
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }
}
