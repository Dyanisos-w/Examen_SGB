import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink

  ]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);

  registerForm = this.fb.group({
    matricule: ['', [Validators.required, Validators.pattern(/^(L|G|S|AG|AL)\d{5}$/)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    terms: [false, Validators.requiredTrue],
  });

  onSubmit(): void {
    if (this.registerForm.invalid) return;
    console.log(this.registerForm.value);
  }
}
