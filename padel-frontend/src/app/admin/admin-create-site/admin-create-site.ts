import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminSiteManagementService } from '../services/admin-site-management.service';
import { NotificationService } from '../../services/notification.service';

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
  private readonly notification = inject(NotificationService);

  isLoading = false;

  form = this.fb.group({
    nom: ['', Validators.required],
    adresse: ['', Validators.required]
  });

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
