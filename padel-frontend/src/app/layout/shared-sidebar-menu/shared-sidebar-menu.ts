import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { SiteDto, SiteService } from '../../services/site.service';
import { AdminSiteContextService } from '../../admin/services/admin-site-context.service';

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
  private readonly siteContext = inject(AdminSiteContextService);

  @Input() isOpen = false;
  @Input() mode: MenuMode = 'public';
  @Output() navigate = new EventEmitter<void>();
  @Output() logoutClick = new EventEmitter<void>();
  /** Rétrocompatibilité : dashboard écoute encore cet événement. */
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
    { label: 'Gérer les fermetures', route: '/admin/closures/new' },
    { label: 'Créer un admin local', route: '/admin/local-admins/new', globalAdminOnly: true },
    { label: 'Créer un site', route: '/admin/sites/new', globalAdminOnly: true }
  ];

  sites: SiteDto[] = [];
  isSiteDropdownOpen = false;

  constructor() {
    this.siteService.getSites().subscribe({
      next: (sites) => (this.sites = sites),
      error: () => (this.sites = [])
    });
  }

  get showAdminSection(): boolean {
    return this.mode === 'admin' || (this.mode === 'user' && this.isAdmin());
  }

  get currentLinks(): NavItem[] {
    return this.mode === 'public' ? this.publicLinks : this.userLinks;
  }

  /** Site actif provenant du service partagé (null = ALL). */
  get activeSiteId(): number | null {
    return this.siteContext.selectedSiteId;
  }

  get selectedSiteLabel(): string {
    const id = this.activeSiteId;
    if (id === null) return 'Global';
    return this.sites.find(s => s.siteId === id)?.nom ?? 'Global';
  }

  isAdmin(): boolean {
    const role = this.readRole();
    return role === 'ROLE_GLOBALADMIN' || role === 'ROLE_LOCALADMIN';
  }

  isGlobalAdmin(): boolean {
    return this.readRole() === 'ROLE_GLOBALADMIN';
  }

  trackByRoute(_: number, item: NavItem): string {
    return item.route;
  }

  /** null = ALL, number = site précis. */
  selectSite(id: number | null): void {
    this.siteContext.select(id);
    this.isSiteDropdownOpen = false;
    this.siteChange.emit(id === null ? 'ALL' : id);
  }

  toggleSiteDropdown(): void {
    this.isSiteDropdownOpen = !this.isSiteDropdownOpen;
  }

  private readRole(): string | null {
    const token = sessionStorage.getItem('access_token');
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]))?.role ?? null;
    } catch {
      return null;
    }
  }
}
