import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

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

  loginForm = this.fb.group({
    matricule: ['', [Validators.required, Validators.pattern(/^(L|G|S|AG|AL)\d{5}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;
    console.log(this.loginForm.value);
  }
}
