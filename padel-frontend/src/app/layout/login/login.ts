import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NgIf
  ]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private cdr = inject(ChangeDetectorRef);
  errorMessage: string | null = null;

  constructor() {
    this.loginForm.valueChanges.subscribe(() => {
      this.errorMessage = null;
    });
  }

  private readRoleFromToken(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.role ?? null;
    } catch {
      return null;
    }
  }

  loginForm = this.fb.group({
    matricule: ['', [
      Validators.required,
      Validators.pattern(/^(?![;\-]).+/)
    ]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    this.errorMessage = null;
    if (this.loginForm.invalid) return;
    console.log(this.loginForm.value);

    const formValue = this.loginForm.value;

    this.http.post<any>(`${environment.apiBaseUrl}/api/auth/login`,{
      username: formValue.matricule,
      password: this.loginForm.value.password
    }).subscribe({
      next:(response) => {
        console.log("token",response);
        sessionStorage.setItem('access_token', response.accessToken);
        sessionStorage.setItem('refresh_token', response.refreshToken);
        const role = this.readRoleFromToken(response.accessToken);
        const isAdmin = role === 'ROLE_GLOBALADMIN' || role === 'ROLE_LOCALADMIN';
        this.router.navigate([isAdmin ? '/admin' : '/home']);
      },
      error:(error) => {
        console.log("login failed with error: ", error);
        this.errorMessage = error.error?.message || error.message || 'Erreur inconnue';
        this.cdr.detectChanges();
      }
    })
  }
}
