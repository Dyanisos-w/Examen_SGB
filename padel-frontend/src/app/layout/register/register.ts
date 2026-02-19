import { Component, inject } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {CommonModule, NgClass} from '@angular/common';
import {disabled} from '@angular/forms/signals';

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

  registerForm = this.fb.group({
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
    console.log(this.registerForm.value);
  }
}
