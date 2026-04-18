import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminUserManagementService } from '../services/admin-user-management.service';
import { SiteDto, SiteService } from '../../services/site.service';

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

  isLoading = false;
  successMatricule: string | null = null;
  errorMessage: string | null = null;
  siteLoadError: string | null = null;
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
        this.siteLoadError = null;
      },
      error: () => {
        this.siteLoadError = 'Impossible de charger les villes pour le moment.';
      }
    });
  }

  onSubmit() {
    this.successMatricule = null;
    this.errorMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nom, prenom, ville, password, confirmPassword } = this.form.getRawValue();
    if (password !== confirmPassword) {
      this.errorMessage = 'Les mots de passe ne correspondent pas.';
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
        this.successMatricule = response.matricule;
        this.form.reset();
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 403) {
          this.errorMessage = 'Acces refuse: seul un GLOBALADMIN peut creer un admin local.';
          return;
        }
        if (error.status === 400) {
          this.errorMessage = 'Donnees invalides. Verifie les champs et la ville.';
          return;
        }
        this.errorMessage = 'Erreur serveur. Reessaie plus tard.';
      }
    });
  }
}
