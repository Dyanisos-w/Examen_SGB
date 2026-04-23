import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SiteDto, SiteService } from '../../services/site.service';

type MenuMode = 'public' | 'user' | 'admin';

type NavItem = {
  label: string;
  route: string;
  exact?: boolean;
  globalAdminOnly?: boolean;
};

@Component({
  selector: 'app-shared-sidebar-menu',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, FormsModule],
  templateUrl: './shared-sidebar-menu.html',
  styleUrl: './shared-sidebar-menu.css'
})
export class SharedSidebarMenu {
  private readonly siteService = inject(SiteService);

  @Input() isOpen = false;
  @Input() mode: MenuMode = 'public';
  @Output() navigate = new EventEmitter<void>();
  @Output() logoutClick = new EventEmitter<void>();
  @Output() siteChange = new EventEmitter<number | 'ALL'>();

  readonly publicLinks: NavItem[] = [
    { label: 'Accueil', route: '/home', exact: true },
    { label: 'Connexion', route: '/login' },
    { label: "S'inscrire", route: '/register' }
  ];

  readonly userLinks: NavItem[] = [
    { label: 'Accueil', route: '/home', exact: true },
    { label: 'Réserver', route: '/reservation' },
    { label: 'Rejoindre', route: '/join-reservation' },
    { label: 'Mes réservations', route: '/my-reservations' }
  ];

  readonly adminLinks: NavItem[] = [
    { label: 'Dashboard admin', route: '/admin', exact: true },
    { label: 'Créer un terrain', route: '/admin/terrains/new' },
    { label: 'Créer un admin local', route: '/admin/local-admins/new', globalAdminOnly: true },
    { label: 'Créer un site', route: '/admin/sites/new', globalAdminOnly: true }

  ];

  get showAdminSection(): boolean {
    return this.mode === 'admin' || (this.mode === 'user' && this.isAdmin());
  }

  get currentLinks(): NavItem[] {
    return this.mode === 'public' ? this.publicLinks : this.userLinks;
  }

  isAdmin(): boolean {
    const token = sessionStorage.getItem('access_token');
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role === 'ROLE_GLOBALADMIN' || payload.role === 'ROLE_LOCALADMIN';
    } catch {
      return false;
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

  trackByRoute(_: number, item: NavItem): string {
    return item.route;
  }
  selectedSiteId: number | 'ALL' = 'ALL';
  sites: SiteDto[] = [];
  isSiteDropdownOpen = false;

  constructor() {
    this.siteService.getSites().subscribe({
      next: (sites) => (this.sites = sites),
      error: () => (this.sites = [])
    });
  }

  get selectedSiteLabel(): string {
    if (this.selectedSiteId === 'ALL') return 'Global';
    const site = this.sites.find(s => s.siteId === this.selectedSiteId);
    return site ? site.nom : 'Global';
  }

  selectSite(id: number | 'ALL'): void {
    this.selectedSiteId = id;
    this.isSiteDropdownOpen = false;
    this.siteChange.emit(id);
  }

  toggleSiteDropdown(): void {
    this.isSiteDropdownOpen = !this.isSiteDropdownOpen;
  }

  onSiteChange(): void {
    this.siteChange.emit(this.selectedSiteId);
  }
}
