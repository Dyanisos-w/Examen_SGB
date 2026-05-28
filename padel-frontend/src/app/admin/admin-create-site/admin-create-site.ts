import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminSiteManagementService } from '../services/admin-site-management.service';
import { NotificationService } from '../../services/notification.service';
import { SharedSidebarMenu } from '../../layout/shared-sidebar-menu/shared-sidebar-menu';
import { AdminTopbar } from '../admin-topbar/admin-topbar';

@Component({
  selector: 'app-admin-create-site',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SharedSidebarMenu, AdminTopbar],
  templateUrl: './admin-create-site.html',
  styleUrl: './admin-create-site.css'
})
export class AdminCreateSiteComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminSiteManagementService = inject(AdminSiteManagementService);
  private readonly notification = inject(NotificationService);
  private readonly router = inject(Router);

  isSidebarOpen = false;
  isLoading = false;

  form = this.fb.group({
    nom: ['', Validators.required],
    adresse: ['', Validators.required]
  });

  toggleSidebar(): void { this.isSidebarOpen = !this.isSidebarOpen; }
  closeSidebar(): void { this.isSidebarOpen = false; }
  logout(): void {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    this.router.navigate(['/login']);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nom, adresse } = this.form.getRawValue();

    this.isLoading = true;
    this.adminSiteManagementService.createSite({
      nom: nom!.trim(),
      adresse: adresse!.trim()
    }).subscribe({
      next: (created) => {
        this.notification.success(`Site créé : ${created.nom} (id: ${created.siteId})`);
        this.form.reset({ nom: '', adresse: '' });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 403) {
          this.notification.error('Accès refusé : seul un GLOBALADMIN peut créer un site.');
          return;
        }
        if (error.status === 400) {
          this.notification.error('Données invalides. Vérifiez les champs saisis.');
          return;
        }
        this.notification.error('Erreur serveur. Réessayez plus tard.');
      }
    });
  }
}
