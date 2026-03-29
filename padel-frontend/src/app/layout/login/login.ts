import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {HttpClient} from '@angular/common/http';

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

  loginForm = this.fb.group({
    matricule: ['', [Validators.required, Validators.pattern(/^(L|G|S|AG|AL)\d{5}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    console.log(this.loginForm.value);

    const formValue = this.loginForm.value;

    this.http.post<any>('http://localhost:8080/api/auth/login',{
      username: formValue.matricule,
      password: this.loginForm.value.password
    }).subscribe({
      next:(response) => {
        console.log("token",response);
        sessionStorage.setItem('access_token', response.accessToken);
        sessionStorage.setItem('refresh_token', response.refreshToken);
        this.router.navigate(['/']);
      },
      error:(error) => {
        console.log("login failed with error: ", error);
        alert("Login failed: " + (error.error?.message || error.statusText || 'Unknown error'));
      }
    })
  }
}
