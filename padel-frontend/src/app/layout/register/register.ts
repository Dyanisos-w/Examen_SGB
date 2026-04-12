import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CommonModule, NgClass } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { SiteDto, SiteService } from '../../services/site.service';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgClass,
    CommonModule
  ]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private siteService = inject(SiteService);

  sites: SiteDto[] = [];
  siteLoadError: string | null = null;

  registerForm = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    matricule: [{ value: '', disabled: true }],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    terms: [false, Validators.requiredTrue],
    accountType: ['', Validators.required],
    ville: [{ value: '', disabled: true }]
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

  selectAccountType(type: string): void {
    this.registerForm.patchValue({ accountType: type });
    const villeControl = this.registerForm.get('ville');
    if (type === 'LOCAL') {
      villeControl?.enable();
      villeControl?.setValidators(Validators.required);
    } else {
      villeControl?.reset();
      villeControl?.clearValidators();
      villeControl?.disable();
    }
    villeControl?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    if (this.registerForm.value.password !== this.registerForm.value.confirmPassword) {
      alert("Passwords don't match");
      return;
    }

    const accountType = this.registerForm.value.accountType;
    const data = {
      nom: this.registerForm.value.nom,
      prenom: this.registerForm.value.prenom,
      password: this.registerForm.value.password,
      accountType,
      ville: accountType === 'LOCAL' ? this.registerForm.value.ville : null
    };

    this.http.post(`${environment.apiBaseUrl}/api/auth/register`, data).subscribe({
      next: () => {
        alert('Successfully registered');
        this.router.navigate(['/login']);
      }
    });
  }
}
