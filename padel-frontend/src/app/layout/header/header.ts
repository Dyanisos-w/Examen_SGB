import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SharedSidebarMenu } from '../shared-sidebar-menu/shared-sidebar-menu';

@Component({
  selector: 'app-header',
  imports: [
    RouterLink,
    RouterLinkActive,
    SharedSidebarMenu
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
