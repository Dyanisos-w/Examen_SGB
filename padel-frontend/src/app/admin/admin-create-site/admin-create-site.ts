import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminSiteManagementService } from '../services/admin-site-management.service';

@Component({
  selector: 'app-admin-create-site',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-create-site.html',
  styleUrl: './admin-create-site.css'
})
export class AdminCreateSiteComponent {
  private readonly fb = inject(FormBuilder);
  private readonly adminSiteManagementService = inject(AdminSiteManagementService);

  isLoading = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  form = this.fb.group({
    nom: ['', Validators.required],
    adresse: ['', Validators.required],
    nombreTerrains: [1, [Validators.required, Validators.min(0)]]
  });

  onSubmit(): void {
    this.successMessage = null;
    this.errorMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { nom, adresse, nombreTerrains } = this.form.getRawValue();

    this.isLoading = true;
    this.adminSiteManagementService.createSite({
      nom: nom!.trim(),
      adresse: adresse!.trim(),
      nombreTerrains: Number(nombreTerrains)
    }).subscribe({
      next: (created) => {
        this.successMessage = `Site cree: ${created.nom} (id: ${created.siteId})`;
        this.form.reset({ nom: '', adresse: '', nombreTerrains: 1 });
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 403) {
          this.errorMessage = 'Acces refuse: seul un GLOBALADMIN peut creer un site.';
          return;
        }
        if (error.status === 400) {
          this.errorMessage = 'Donnees invalides. Verifie les champs saisis.';
          return;
        }
        this.errorMessage = 'Erreur serveur. Reessaie plus tard.';
      }
    });
  }
}

