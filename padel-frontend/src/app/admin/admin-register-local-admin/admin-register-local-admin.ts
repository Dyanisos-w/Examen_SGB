import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminUserManagementService } from '../services/admin-user-management.service';
import { SiteDto, SiteService } from '../../services/site.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-admin-register-local-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-register-local-admin.html',
  styleUrl: './admin-register-local-admin.css'
})
export class AdminRegisterLocalAdminComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminUserManagementService = inject(AdminUserManagementService);
  private readonly siteService = inject(SiteService);
  private readonly notification = inject(NotificationService);

  isLoading = false;
  sites: SiteDto[] = [];

  form = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    ville: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required]
  });

  constructor() {
    this.loadSites();
  }

  private loadSites(): void {
    this.siteService.getSites().subscribe({
      next: (sites) => {
        this.sites = sites;
      },
      error: () => {
        this.notification.error('Impossible de charger les villes pour le moment.');
      }
    });
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nom, prenom, ville, password, confirmPassword } = this.form.getRawValue();
    if (password !== confirmPassword) {
      this.notification.error('Les mots de passe ne correspondent pas.');
      return;
    }

    this.isLoading = true;
    this.adminUserManagementService.createLocalAdmin({
      nom: nom!.trim(),
      prenom: prenom!.trim(),
      ville: ville!,
      password: password!
    }).subscribe({
      next: (response) => {
        this.notification.success(`Admin local créé avec succès. Matricule : ${response.matricule}`, 0);
        this.form.reset();
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 403) {
          this.notification.error('Accès refusé : seul un GLOBALADMIN peut créer un admin local.');
          return;
        }
        if (error.status === 400) {
          this.notification.error('Données invalides. Vérifiez les champs et la ville.');
          return;
        }
        this.notification.error('Erreur serveur. Réessayez plus tard.');
      }
    });
  }
}
