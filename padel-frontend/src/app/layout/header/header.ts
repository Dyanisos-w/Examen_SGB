import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SharedSidebarMenu } from '../shared-sidebar-menu/shared-sidebar-menu';
import {NgOptimizedImage} from '@angular/common';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    RouterLinkActive,
    SharedSidebarMenu,
    NgOptimizedImage
  ],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private router = inject(Router);
  isMenuOpen = false;

  get isLoggedIn(): boolean {
    return !!sessionStorage.getItem('access_token');
  }

  get isAdmin(): boolean {
    const token = sessionStorage.getItem('access_token');
    if (!token) return false;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const role = payload?.role ?? '';
      return role === 'ROLE_GLOBALADMIN' || role === 'ROLE_LOCALADMIN';
    } catch {
      return false;
    }
  }

  get mobileMenuMode(): 'public' | 'user' {
    return this.isLoggedIn ? 'user' : 'public';
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen;
  }

  closeMenu(): void {
    this.isMenuOpen = false;
  }

  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.closeMenu();
    this.router.navigate(['/login']);
  }
}
