import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {HttpClient} from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private http = inject(HttpClient);
  private router = inject(Router);
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
        alert("Login failed: " + (error.error?.message || error.statusText || 'Unkwown user'));
      }
    })
  }
}
