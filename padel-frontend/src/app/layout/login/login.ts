import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { NotificationService } from '../../services/notification.service';
import { ToastComponent } from '../toast/toast';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ToastComponent]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
  private notification = inject(NotificationService);

  private readRoleFromToken(token: string): string | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.role ?? null;
    } catch {
      return null;
    }
  }

  loginForm = this.fb.group({
    matricule: ['', [Validators.required, Validators.pattern(/^(L|G|S|GA|LA)\d{5}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    const formValue = this.loginForm.value;

    this.http.post<any>(`${environment.apiBaseUrl}/api/auth/login`, {
      username: formValue.matricule,
      password: this.loginForm.value.password
    }).subscribe({
      next: (response) => {
        sessionStorage.setItem('access_token', response.accessToken);
        sessionStorage.setItem('refresh_token', response.refreshToken);
        const role = this.readRoleFromToken(response.accessToken);
        const isAdmin = role === 'ROLE_GLOBALADMIN' || role === 'ROLE_LOCALADMIN';
        this.router.navigate([isAdmin ? '/admin' : '/home']);
      },
      error: (error) => {
        this.notification.error('Login incorrect : ' + (error.error?.message || error.statusText || 'Identifiants invalides'));
      }
    });
  }
}
