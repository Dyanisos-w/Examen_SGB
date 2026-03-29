import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {CommonModule, NgClass} from '@angular/common';
import {disabled} from '@angular/forms/signals';
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";

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
  registerForm = this.fb.group({
    nom: ['', Validators.required],
    prenom: ['', Validators.required],
    matricule: [{value: '', disabled: true}],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    terms: [false, Validators.requiredTrue],
    accountType: ['', Validators.required],
    ville: [{value: '', disabled: true},],


  });
  selectAccountType(type: string): void {
    this.registerForm.patchValue({ accountType: type });
    const villeControl = this.registerForm.get('ville');
  if (type=== 'LOCAL') {
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

    if (this.registerForm.value.password !== this.registerForm.value.confirmPassword)
    {
      alert("Passwords don't match");
      return;
    }

    const data = {
      nom: this.registerForm.value.nom,
      password: this.registerForm.value.password,
      accountType: this.registerForm.value.accountType,
      ville: this.registerForm.value.ville,

    }

    this.http.post('http://localhost:8080/api/auth/register', data).subscribe({
      next: () => {
        alert("Successfully registered");
        this.router.navigate(['/login']);
      }
    });
  }
}
